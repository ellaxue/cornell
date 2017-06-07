# File: motion_pedestrian.py
# This script detects motion and then triggers human body detection without DropBox uploading.
# Author: Albert Xu, Ella Xue, Danna Ma
# Date: May 6, 2017


from picamera import PiCamera
from picamera.array import PiRGBArray
from imutils.object_detection import non_max_suppression
from imutils import paths

import argparse
import warnings
import datetime
import imutils
import json

import multiprocessing as mp
import numpy as np
import os
import time
import sys
import cv2

FACE_SKIP_COUNT = 3
PED_SKIP_COUNT = 12


#################################
# Setup
#################################

cascade_path = "haarcascade_frontalface_default.xml"

# camera initalization
camera = PiCamera()
camera.resolution = (640, 480)
camera.framerate = 30
raw_capture = PiRGBArray(camera, size=(640, 480))

# initalize Haar cascade
face_cascade = cv2.CascadeClassifier(cascade_path)

# initialize the HOG descriptor/person detector
hog = cv2.HOGDescriptor()
hog.setSVMDetector(cv2.HOGDescriptor_getDefaultPeopleDetector())

proc_rr = 1
begin = 0
running_ped = 0
face_skip = 0
ped_skip = 0
detected = 0

avg = None
ap = argparse.ArgumentParser()
ap.add_argument("-c", "--conf", required=True,
	help="path to the JSON configuration file")
args = vars(ap.parse_args())
conf = json.load(open(args["conf"]))

time.sleep(0.1) # warmup time

ped_on = int(raw_input("Enter 1 to enable pedestrian detection, 0 to disable: "))


#################################
# Functions
#################################

def face_detect(image):
	faces = face_cascade.detectMultiScale(
		image,
		scaleFactor=1.3,
		minNeighbors=5,
		minSize=(30, 30)
	)
	try:
		if faces.size != 0:
			detected = 1
	except:
		detected = 0
	return faces, detected


def ped_detect(image):
	(rects, weights) = hog.detectMultiScale(
		image,
		winStride=(4, 4),
		padding=(8, 8),
		scale=1.05
	)
	return (rects, weights)


def non_max_supp(rects):
	rects = np.array([[x, y, x + w, y + h] for (x, y, w, h) in rects])
	pick = non_max_suppression(rects, probs=None, overlapThresh=0.65)
	return pick


def draw_rect(rects, image):
	for (x, y, w, h) in rects:
		cv2.rectangle(image, (x, y), (x+w, y+h), (0, 255, 0), 2)


#################################
# Main Code
#################################

if __name__ == '__main__':

	proc_pool = mp.Pool(processes = 4)

	for frame in camera.capture_continuous(raw_capture, format="bgr", use_video_port=True):

		# face skip logic
		if face_skip >= FACE_SKIP_COUNT:
			face_skip = 0
		else:
			face_skip += 1
		if face_skip != 0:
			raw_capture.truncate(0)
			continue

		running_ped = 0;
		text = "Unoccupied"

		# input_image is frame from camera, gray is grayscale version for Haar classifier
		input_image = frame.array
		imput_image = imutils.resize(input_image, width = 500)
		gray = cv2.cvtColor(input_image, cv2.COLOR_BGR2GRAY)
		gray = cv2.GaussianBlur(gray,(21,21),0)
		# if the average frame is None, initialize it
		if avg is None:
			print("[INFO] starting background model...")
			avg = gray.copy().astype("float")
			raw_capture.truncate(0)
			continue

		# accumulate the weighted average between the current frame and
		# previous frames, then compute the difference between the current
		# frame and running average
		cv2.accumulateWeighted(gray, avg, 0.5)
		frameDelta = cv2.absdiff(gray, cv2.convertScaleAbs(avg))

		# threshold the delta image, dilate the thresholded image to fill
		# in holes, then find contours on thresholded image
		thresh = cv2.threshold(frameDelta, conf["delta_thresh"], 255,
			cv2.THRESH_BINARY)[1]
		thresh = cv2.dilate(thresh, None, iterations=2)
		(cnts,_) = cv2.findContours(thresh.copy(), cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_SIMPLE)

		# loop over the contours
		for c in cnts:
			# if the contour is too small, ignore it
			if cv2.contourArea(c) < conf["min_area"]:
				continue
			text = "Occupied"

		timestamp = datetime.datetime.now()
		ts = timestamp.strftime("%A %d %B %Y %I:%M:%S%p")
		cv2.putText(input_image, "Room Status: {}".format(text), (10, 20),
			cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 255), 2)
		cv2.putText(input_image, ts, (10, input_image.shape[0] - 10), cv2.FONT_HERSHEY_SIMPLEX,
			0.35, (0, 0, 255), 1)

		if text == "Occupied":
			detected = 1

		running_ped = detected
		if not ped_on:
			running_ped = 0

		# if face has been detected and frames skipped, run pedestrian detector
		if running_ped and ped_skip > PED_SKIP_COUNT:
			print("Running pedestrian detector")
			(rects, weights) = ped_detect(input_image)

			draw_rect(rects, input_image)
			pick = non_max_supp(rects)
			for (x1, y1, x2, y2) in pick:
				cv2.rectangle(input_image, (x1, y1), (x2, y2), (0, 255, 0), 2)
			ped_skip = 0
		# end if running_ped

		# show the frame
		cv2.imshow("Frame", input_image)
		key = cv2.waitKey(1) & 0xFF

		# increment variables
		proc_rr += 1
		ped_skip += 1

		# clear the stream in preparation for the next frame
		raw_capture.truncate(0)

		# if the `q` key was pressed, break from the loop
		if key == ord("q"):
			break


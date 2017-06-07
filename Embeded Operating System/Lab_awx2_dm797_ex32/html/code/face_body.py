# File: face_body.py
# This script detect human face and body using the Haar cascade classifiers.
# The detecting accuracy is relatively low compared to HOG pedestrian detector.
#
# Author: Albert Xu, Ella Xue, and Danna Ma
# Date: May 6, 2017


from picamera import PiCamera
from picamera.array import PiRGBArray

import multiprocessing as mp
import numpy as np
import os
import time
import sys
import cv2

NUM_PROCS = 4


#################################
# Setup
#################################

face_cascade_path = "haarcascade_frontalface_default.xml"
lower_body_cascade_path = "haarcascade_lowerbody.xml"
upper_body_cascade_path = "haarcascade_lowerbody.xml"

# camera initalization
camera = PiCamera()
camera.resolution = (640, 480)
camera.framerate = 30
raw_capture = PiRGBArray(camera, size=(640, 480))

# Haar cascades
face_cascade = cv2.CascadeClassifier(face_cascade_path)
lower_body_cascade = cv2.CascadeClassifier(lower_body_cascade_path)
upper_body_cascade = cv2.CascadeClassifier(upper_body_cascade_path)

proc_rr = 0
begin = 0

time.sleep(0.1) # warmup time


#################################
# Functions
#################################

def face_detect(gray):
	faces = face_cascade.detectMultiScale(
		gray,
		scaleFactor=1.1,
		minNeighbors=5,
		minSize=(30, 30)
	)
	return faces

def lower_body_detect(gray):
	lower_bodies = lower_body_cascade.detectMultiScale(
		gray,
		scaleFactor=1.1,
		minNeighbors=5,
		minSize=(30, 30)
	)
	return lower_bodies

def upper_body_detect(gray):
	upper_bodies = upper_body_cascade.detectMultiScale(
		gray,
		scaleFactor=1.1,
		minNeighbors=5,
		minSize=(30, 30)
	)
	return upper_bodies

def detect(gray):
	faces = face_detect(gray);
	lower_bodies = lower_body_detect(gray);
	upper_bodies = upper_body_detect(gray);
	return faces, lower_bodies, upper_bodies;


#################################
# Main Code
#################################

if __name__ == '__main__':

	proc_pool = mp.Pool(processes = NUM_PROCS)

	for frame in camera.capture_continuous(raw_capture, format="bgr", use_video_port=True):

		# input_image is frame from camera
		input_image = frame.array

		# create copy arrays
		blurred_image = input_image
		cv2.medianBlur(input_image, 3, blurred_image)

		gray = cv2.cvtColor(blurred_image, cv2.COLOR_BGR2GRAY)

		# round robin calls to each processor
		if proc_rr == 0:
			t0 = proc_pool.apply_async(detect, [gray])
			if begin:
				faces, lower_bodies, upper_bodies = t1.get()

		elif proc_rr == 1:
			t1 = proc_pool.apply_async(detect, [gray])
			if begin:
				faces, lower_bodies, upper_bodies = t2.get()

		elif proc_rr == 2:
			t2 = proc_pool.apply_async(detect, [gray])
			if begin:
				faces, lower_bodies, upper_bodies = t3.get()

		elif proc_rr == 3:
			t3 = proc_pool.apply_async(detect, [gray])
			if begin:
				faces, lower_bodies, upper_bodies = t0.get()

		if begin:
			for (x, y, w, h) in faces:
				cv2.rectangle(input_image, (x, y), (x+w, y+h), (0, 255, 0), 2)
			for (x, y, w, h) in lower_bodies:
				cv2.rectangle(input_image, (x, y), (x+w, y+h), (0, 255, 0), 2)
			for (x, y, w, h) in upper_bodies:
				cv2.rectangle(input_image, (x, y), (x+w, y+h), (0, 255, 0), 2)

		# serial section
		proc_rr += 1

		if proc_rr == 4:
			begin = 1
			proc_rr = 0

		# show the frame
		cv2.imshow("Frame", input_image)
		key = cv2.waitKey(1) & 0xFF

		# clear the stream in preparation for the next frame
		raw_capture.truncate(0)

		# if the `q` key was pressed, break from the loop
		if key == ord("q"):
			break


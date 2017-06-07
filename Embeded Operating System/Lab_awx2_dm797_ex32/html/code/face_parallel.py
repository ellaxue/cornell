# File: face_parallel.py
# This script detect human faces with multi-threading running on the 4 cores.
# Authors: Albert Xu, Ella Xue, and Danna Ma
# Date: May 6, 2017


from picamera import PiCamera
from picamera.array import PiRGBArray

import multiprocessing as mp
import os
import time
import sys
import cv2

NUM_PROCS = 4


#################################
# Setup
#################################

cascPath = "haarcascade_frontalface_default.xml"

# camera initalization
camera = PiCamera()
camera.resolution = (640, 480)
camera.framerate = 30
rawCapture = PiRGBArray(camera, size=(640, 480))

# Haar cascade
faceCascade = cv2.CascadeClassifier(cascPath)

proc_rr = 0
begin = 0

time.sleep(0.1) # warmup time


#################################
# Functions
#################################

def face_detect(gray):
	faces = faceCascade.detectMultiScale(
		gray,
		scaleFactor=1.1,
		minNeighbors=5,
		minSize=(30, 30)
	)
	return faces


#################################
# Main Code
#################################

if __name__ == '__main__':

	proc_pool = mp.Pool(processes = NUM_PROCS)

	for frame in camera.capture_continuous(rawCapture, format="bgr", use_video_port=True):

		# input_image is frame from camera
		input_image = frame.array

		# create copy arrays
		blurred_image = input_image
		cv2.medianBlur(input_image, 3, blurred_image)

		gray = cv2.cvtColor(blurred_image, cv2.COLOR_BGR2GRAY)

		# round robin calls to each processor
		if proc_rr == 0:
			t0 = proc_pool.apply_async(face_detect, [gray])
			if begin:
				f1 = t1.get()
				for (x, y, w, h) in f1:
					cv2.rectangle(input_image, (x, y), (x+w, y+h), (0, 255, 0), 2)

		elif proc_rr == 1:
			t1 = proc_pool.apply_async(face_detect, [gray])
			if begin:
				f2 = t2.get()
				for (x, y, w, h) in f2:
					cv2.rectangle(input_image, (x, y), (x+w, y+h), (0, 255, 0), 2)

		elif proc_rr == 2:
			t2 = proc_pool.apply_async(face_detect, [gray])
			if begin:
				f3 = t3.get()
				for (x, y, w, h) in f3:
					cv2.rectangle(input_image, (x, y), (x+w, y+h), (0, 255, 0), 2)

		elif proc_rr == 3:
			t3 = proc_pool.apply_async(face_detect, [gray])
			if begin:
				f0 = t0.get()
				for (x, y, w, h) in f0:
					cv2.rectangle(input_image, (x, y), (x+w, y+h), (0, 255, 0), 2)
			proc_rr = 0;
			begin = 1

		# serial section
		proc_rr += 1

		# show the frame
		cv2.imshow("Frame", input_image)
		key = cv2.waitKey(1) & 0xFF

		# clear the stream in preparation for the next frame
		rawCapture.truncate(0)

		# if the `q` key was pressed, break from the loop
		if key == ord("q"):
			break


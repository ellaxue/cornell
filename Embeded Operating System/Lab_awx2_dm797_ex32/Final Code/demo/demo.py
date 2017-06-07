# File: demo.py
# Author: Albert Xu, Ella Xue, and Danna Ma
# Date: May 17, 2017


from picamera import PiCamera
from picamera.array import PiRGBArray
import RPi.GPIO as GPIO
from imutils.object_detection import non_max_suppression
from imutils import paths

import datetime
import imutils
import time
import multiprocessing as mp
import numpy as np
import os
import sys
import math
import cv2

SCREEN_WIDTH = 640
SCREEN_HEIGHT = 480

FACE_SKIP_COUNT = 3
PED_SKIP_COUNT = 30
TRACKING_BOX = 80
TRACK_INCREMENT = 0.12
MANUAL_INCREMENT = 0.3

# set up pinout
GPIO.setmode(GPIO.BCM)
GPIO.setup(5, GPIO.OUT)
GPIO.setup(17, GPIO.OUT)

# initalize servos
f = 50
x_servo = GPIO.PWM(5, f)
y_servo = GPIO.PWM(17, f)
x_servo.start(0)
y_servo.start(0)

# camera initalization
camera = PiCamera()
camera.resolution = (SCREEN_WIDTH, SCREEN_HEIGHT)
camera.framerate = 30
raw_capture = PiRGBArray(camera, size=(SCREEN_WIDTH, SCREEN_HEIGHT))

# initalize Haar cascade
cascade_path = "haarcascade_frontalface_default.xml"
face_cascade = cv2.CascadeClassifier(cascade_path)

# initialize the HOG descriptor/person detector
hog = cv2.HOGDescriptor()
hog.setSVMDetector(cv2.HOGDescriptor_getDefaultPeopleDetector())

# initalize variables
proc_rr = 1
begin = 0
running_ped = 0
face_skip = 0
ped_skip = 0
detected = 0
detected_faces = 0
x_servo_dir = 5
y_servo_dir = 5
x_sat_counter = 2
y_sat_counter = 2
tracking_on = 1 # 1 for tracking, 0 for manual control
ped_on = 0 # 1 to turn on pedestrian detection, 0 to disable

start = time.time()

#################################
# Setup
#################################

# bring servos to inital orientation
x_servo.ChangeDutyCycle(x_servo_dir)
y_servo.ChangeDutyCycle(y_servo_dir)
time.sleep(0.4)
x_servo.ChangeDutyCycle(0)
y_servo.ChangeDutyCycle(0)


#################################
# Functions
#################################

def face_track(detected, faces):

	global x_servo_dir
	global y_servo_dir
	global x_sat_counter
	global y_sat_counter

	if not detected: # if not detected, do not track
		x_servo.ChangeDutyCycle(0)
		y_servo.ChangeDutyCycle(0)

	else: # if detected, turn towards face
		x_center = faces[0][0] + faces[0][2]/2
		y_center = faces[0][1] + faces[0][3]/2

		# compute saturating counters
		if x_center - SCREEN_WIDTH/2 > TRACKING_BOX:
			x_sat_counter -= 1
		elif SCREEN_WIDTH/2 - x_center > TRACKING_BOX:
			x_sat_counter += 1
		if y_center - SCREEN_HEIGHT/2 > TRACKING_BOX:
			y_sat_counter += 1
		elif SCREEN_HEIGHT/2 - y_center > TRACKING_BOX:
			y_sat_counter -= 1

		if x_sat_counter < 0: # turn right
			x_sat_counter = 0
			x_servo_dir -= TRACK_INCREMENT

		elif x_sat_counter > 5: # turn left
			x_sat_counter = 5
			x_servo_dir += TRACK_INCREMENT

		if y_sat_counter > 5: # turn down
			y_sat_counter = 5
			y_servo_dir += TRACK_INCREMENT

		elif y_sat_counter < 0: # turn up
			y_sat_counter = 0
			y_servo_dir -= TRACK_INCREMENT

		# bounds checking
		if x_servo_dir < 2:
			x_servo_dir = 2
		if x_servo_dir > 12:
			x_servo_dir = 12
		if y_servo_dir < 2:
			y_servo_dir = 2
		if y_servo_dir > 12:
			y_servo_dir = 12

		# move servos, then power off PWMs
		x_servo.ChangeDutyCycle(x_servo_dir)
		y_servo.ChangeDutyCycle(y_servo_dir)
		time.sleep(0.2)
		x_servo.ChangeDutyCycle(0)
		y_servo.ChangeDutyCycle(0)


def face_detect(image):
	faces = face_cascade.detectMultiScale(
		image,
		scaleFactor=1.3,
		minNeighbors=5,
		minSize=(30, 30))
	try: # if faces is nonzero, at least one face is detected
		if faces.size != 0:
			detected = 1
	except: # no faces detected
		detected = 0
	return faces, detected


def ped_detect(image):
	(rects, weights) = hog.detectMultiScale(
		image,
		winStride=(4, 4),
		padding=(8, 8),
		scale=1.05)
	return (rects, weights)


def non_max_supp(rects):
	rects = np.array([[x, y, x + w, y + h] for (x, y, w, h) in rects])
	pick = non_max_suppression(rects, probs=None, overlapThresh=0.65)
	return pick


def draw_rect(rects, image):
	for (x, y, w, h) in rects:
		cv2.rectangle(image, (x, y), (x+w, y+h), (0, 255, 0), 2)


#################################
# Main Function
#################################

if __name__ == '__main__':

	proc_pool = mp.Pool(processes = 4)

	for frame in camera.capture_continuous(raw_capture, format="bgr", use_video_port=True):
		timestamp = datetime.datetime.now()

		# face skip logic
		if face_skip >= FACE_SKIP_COUNT:
			face_skip = 0
		else:
			face_skip += 1
		if face_skip != 0: # skip this frame by clearing the buffer and continuing
			raw_capture.truncate(0)
			continue

		running_ped = 0;

		# input_image is frame from camera, gray is grayscale version for Haar classifier
		input_image = frame.array
		gray = cv2.cvtColor(input_image, cv2.COLOR_BGR2GRAY)

		# round robin calls to each processor for multiprocessing
		if proc_rr == 1:
			t0 = proc_pool.apply_async(face_detect, [gray])
			if begin:
				f1, detected = t1.get()
				draw_rect(f1, input_image)
				detected_faces = f1

		elif proc_rr == 2:
			t1 = proc_pool.apply_async(face_detect, [gray])
			if begin:
				f2, detected = t2.get()
				draw_rect(f2, input_image)
				detected_faces = f2

		elif proc_rr == 3:
			t2 = proc_pool.apply_async(face_detect, [gray])
			if begin:
				f3, detected = t3.get()
				draw_rect(f3, input_image)
				detected_faces = f3

		elif proc_rr == 4:
			t3 = proc_pool.apply_async(face_detect, [gray])
			if begin:
				f0, detected = t0.get()
				draw_rect(f0, input_image)
				detected_faces = f0
			proc_rr = 0;
			begin = 1

		face_track(detected, detected_faces)

		running_ped = detected # if detected a face, run pedestrian detector unless...
		if not ped_on: # skip pedestrian detector and Dropbox upload
			running_ped = 0
			if detected:
				upload = 1

		# if face has been detected and frames skipped, run pedestrian detector
		if running_ped and ped_skip > PED_SKIP_COUNT:
			(rects, weights) = ped_detect(input_image)
			draw_rect(rects, input_image)
			pick = non_max_supp(rects)
			for (x1, y1, x2, y2) in pick:
				cv2.rectangle(input_image, (x1, y1), (x2, y2), (0, 255, 0), 2)
			ped_skip = 0
			if len(rects) > 0:
				upload = 1
		# end if running_ped

		# show the frame
		cv2.imshow("Frame", input_image)
		key = cv2.waitKey(1) & 0xFF

		# increment variables
		proc_rr += 1
		ped_skip += 1

		# clear the stream in preparation for the next frame
		raw_capture.truncate(0)

		if key == ord("q"):
			break

		end = time.time()

		elapsed_time = end - start
		if elapsed_time > 60:
			break

	# end camera for loop

x_servo.stop() # goodbye
y_servo.stop()
GPIO.cleanup()
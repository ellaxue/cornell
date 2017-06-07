# File: face_ped_db_track.py
# This script detect human faces and bodies use Harr cascade and HOG pedestrain respectively.
# The captured images of detected humans will be uploaded to the DropBox and email to users.
# Author: Albert Xu, Ella Xue, and Danna Ma
# Date: May 16, 2017


from picamera import PiCamera
from picamera.array import PiRGBArray
import RPi.GPIO as GPIO
from imutils.object_detection import non_max_suppression
from imutils import paths

import argparse
from pyimagesearch.tempimage import TempImage
from dropbox.client import DropboxOAuth2FlowNoRedirect
from dropbox.client import DropboxClient
import warnings
import datetime
import imutils
import json
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


#################################
# Setup
#################################

# construct the argument parser and parse the arguments
ap = argparse.ArgumentParser()
ap.add_argument("-c", "--conf", required=True,
	help="path to the JSON configuration file")
args = vars(ap.parse_args())

# filter warnings, load the configuration and initialize the Dropbox client
warnings.filterwarnings("ignore")
conf = json.load(open(args["conf"]))
client = None

# check to see if the Dropbox should be used
if conf["use_dropbox"]:
	# connect to dropbox and start the session authorization process
	flow = DropboxOAuth2FlowNoRedirect(conf["dropbox_key"], conf["dropbox_secret"])
	print "[INFO] Authorize this application: {}".format(flow.start())
	authCode = raw_input("Enter auth code here: ").strip()

	# finish the authorization and grab the Dropbox client
	(accessToken, userID) = flow.finish(authCode)
	client = DropboxClient(accessToken)
	print "[SUCCESS] dropbox account linked"

# allow the camera to warmup, then initialize the average frame, last
# uploaded timestamp, and frame motion counter
print "[INFO] warming up..."
time.sleep(conf["camera_warmup_time"])
avg = None
lastUploaded = datetime.datetime.now()
upload = 0

# print user instructions
print("")
print("Camera tracking is ON. Press t to toggle")
print("Pedestrian detection is OFF. Press r to toggle")

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


def turn_mount(direction):

	global x_servo_dir
	global y_servo_dir

	# update servo directions based on keystrokes
	if direction == "right":
		x_servo_dir -= MANUAL_INCREMENT
	elif direction == "left":
		x_servo_dir += MANUAL_INCREMENT
	elif direction == "down":
		y_servo_dir += MANUAL_INCREMENT
	elif direction == "up":
		y_servo_dir -= MANUAL_INCREMENT

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

		if upload == 1 and (timestamp - lastUploaded).seconds >= conf["min_upload_seconds"] :
			# check to see if dropbox should be used
			if conf["use_dropbox"]:                    
				ts = timestamp.strftime("%A %d %B %Y %I:%M:%S%p")	
				# write the image to temporary file
				t = TempImage()
				cv2.imwrite(t.path, input_image)

				# upload the image to Dropbox and cleanup the tempory image
				print "[UPLOAD] {}".format(ts)
				path = "{base_path}/{timestamp}.jpg".format(base_path=conf["dropbox_base_path"], timestamp=ts)
				client.put_file(path, open(t.path, "rb"))
				t.cleanup()

				# update the last uploaded timestamp and reset the motion counter
				lastUploaded = timestamp
				upload = 0

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

		if tracking_on: # automatic face tracking
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

		if not tracking_on:
			if key == ord("a"):
				turn_mount("left")
			elif key == ord("d"):
				turn_mount("right")
			elif key == ord("w"):
				turn_mount("up")
			elif key == ord("s"):
				turn_mount("down")

		if key == ord("q"):
			break
		elif key == ord("t"):
			tracking_on = 1 - tracking_on # toggle tracking mode
		elif key == ord("r"):
			ped_on = 1 - ped_on # toggle pedestrian detector

	# end camera for loop

x_servo.stop() # goodbye
y_servo.stop()
GPIO.cleanup()
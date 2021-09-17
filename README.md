
![](./ss/mockup.png)

# ✨ MiniSci
- AI based mobile app that aims to improve learning experience for children 🎈
- MiniSci is a proof-of-concept for the ability of improving learning experience using AI
- Made with a lot of love 💗

## 💼 Used structures
### 💣 AI
- Face recognition: FaceNet
- Similarity criteria: Cosine distance 
- Object Detection : YOLO V4 Tiny
- Face detection: Blaze Face

### 📱 Android
- Shared preferences
- CameraX
- Firebase database
- Firebase MLKit
- Tflite interpreter
- Notifications
- Broadcast Reciever
- RecyclerView

## 👮‍♀️ Face Recognition
🎉 The purpose of this method is to make the whole experience more funny for the child

### 🧙‍♀️ Signup Process
- A face detection model (blaze face) works over every frame and returns the coordinates of every detected face.
- The child selects the best second, clicks on + button, the frame is cropped and shown on a dialog with an editText, the child writes his name and clicks on OK
- Using FaceNet a 128 length vector is created and saved as sharedPrefernces to be used in login process

<div float="left">
<img src="./ss/signup.jpeg" width="200"  />
<img src="./ss/signupdialog.jpeg" width="200"  />
<img src="./ss/main.jpeg" width="200"  />
<div>
  
### 👤 Login Process
- A face detection model (Blaze face) detects faces in the frames, finds each vector corresponded to each detected face, calculates cosine distance with vector stored in sharedPrefernces, finds the registered user and pass to dashboard activity. 
- The last detected face is also used for emotion analysis using the ststus of eyes and mouth and every face is classified as `Neutral`, `Happy` or `Drowsy` and different welcome dialogs are shown correspondingly.
  
<div float="left">
<img src="./ss/looking.jpeg" width="200"  />
<img src="./ss/emotion.png" width="380"  />
<div>



## 🌸 Dashboard Activity
Quote and Info of the day with inspirational photos 

<div float="left">
<img src="./ss/panel1.jpeg" width="200"  />
<img src="./ss/panel2.jpeg" width="200"  />
<img src="./ss/menu.jpeg" width="200"  />
<div>


## 🕵️‍♀️ Explore Activity
- A YOLO v4 tiny model analyses the frames, finds the objects in classes it knows and returns coordinates of them
- When the child clicks on the found object all the info related to that object are fetched from Firebase and shown inside a RecyclerView 
- Classes:
  - bird, cat, dog, horse, sheep, cow, elephant, bear, giraffe, banana, apple, orange, broccoli, Carrot, chicken, fish, grape
- Dataset sources:
  - MS COCO
  - Google OpenImages

> 🤖 The model is trained specifically for this project, (instruction document is being prepared, **stay tuned 🥳**)

<div float="left">
<img src="./ss/grapes.jpeg" width="200"  />
<img src="./ss/banana.gif" width="200"  />
<div>


## 🤓 Learn Language Activity
- A YOLO v4 tiny model analyses the frames, finds the objects in classes it knows and returns coordinates of them
- Every detected object is shown in both English and Turkish 
- Classes:
  - COCO's 80 classes
- Dataset sources:
  - MS COCO

<div float="left">
<img src="./ss/laptop.jpeg" width="200"  />
<img src="./ss/lang.gif" width="200"  />
<div>

## 👩‍🏫 Solve Quiz Activity
- A quiz is feteched from Firebase and when the child clicks on option he thinks it's true the result is shown by options' colors

<div float="left">
<img src="./ss/quiz.jpeg" width="200"  />
<img src="./ss/quizres.jpeg" width="200"  />
<img src="./ss/quiz.gif" width="200"  />
<div>

## 📚 Info Activity
- An info is feteched from Firebase and shown in a sweet (🤭) interface
- Every 2 hours an info is shown as a notification

<div float="left">
<img src="./ss/info.jpeg" width="200"  />
<img src="./ss/not.jpeg" width="200"  />
<div>


## 🥰 Quote Activity
- "Educating the mind without educating the heart is no education at all"
- A quote is feteched from Firebase and shown in an interface
- MinSci aims to educate children both scitifically and emotionaly so in this interface we aim to inspire the child and motivate him to be a better **HUMAN**

<div float="left">
<img src="./ss/quote.jpeg" width="200"  />
<div>

## 👩‍🎨 Logo 

- 🔵: `#66bded`
- 🟢: `#8fdd3c`
- 🟠: `#f47340`
- ⚪️: `#f7f7f7`

<div float="left">
<img src="./ss/minisci_withback.png" width="150"  />
<div>

## 🚩 Note 
80% of codes are written in 20 days so code structures aren't well optimized 🤸‍♀️

## 👜 Contact
Find me on [LinkedIn](https://www.linkedin.com/in/asmaamirkhan/) and feel free to mail me, [Asmaa](mailto:asmaamirkhan.am@gmail.com) 🦋 and don't forget to take a look at [asmaamir.com 🥰](https://asmaamir.com/)


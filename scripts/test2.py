import time

text = 'Worker 2 message number: '
num = 0

while True:

    num= num +1
    print text +str(num)

    open("test","wb").write(text + str(num))

    time.sleep(1)
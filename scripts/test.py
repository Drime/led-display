import time

text = 'Worker 1 message number: '
num = 0

while True:

    num = num +1
    print text + str(num)

    time.sleep(1)
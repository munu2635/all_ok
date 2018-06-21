import subprocess

result = subprocess.check_output('cat /dev/ttyUSB0', shell = True)

try :
	if result != null :
		print(result)

	else :
		print("result is null")


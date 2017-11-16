del HaysUCMDeployerLog.log
del instructions.xml
java DeployerConfig>>instructions.xml
pause
rmdir D:\DeployerTemp\ /s /q
mkdir D:\DeployerTemp
start D:\DeployerTemp
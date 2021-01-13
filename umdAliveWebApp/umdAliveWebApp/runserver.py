"""
This script runs the umdAliveWebApp application using a development server.
"""

from os import environ
from umdAliveWebApp import app

# This boolean is for ease of use of debugging
# let serverUse be false for testing on local host
# let serverUse be true for actually hosting the server on ukko

serverUse = True

if serverUse:
    hostName = 'ukko.d.umn.edu'
    portNumber =32895 
else:     
    hostName = 'localhost'
    portNumber = 5555
if __name__ == '__main__':
    #app.run(threadable=True)
    app.run()
    HOST = environ.get('SERVER_HOST', hostName)
    try:
        PORT = int(environ.get('SERVER_PORT', portNumber))
    except ValueError:
        PORT = 45444
    app.run(HOST, PORT)

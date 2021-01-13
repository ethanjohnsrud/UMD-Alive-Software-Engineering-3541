"""
Routes and views for the flask application.
"""

from datetime import datetime
import flask
from flask import render_template,request,redirect,flash
from umdAliveWebApp import app
import requests
from requests.auth import HTTPDigestAuth                              # GET and POST requests
import json                                                           # Parsing JSON files
from flask import Flask, redirect, url_for, session, request, jsonify
from flask_oauthlib.client import OAuth                               # Google Sign-in
import logging                                                        # Debugging
import httplib2                                                       # GET and POST requests
from apiclient.discovery import build                                 # Token authentication for Google
from oauth2client.client import OAuth2WebServerFlow
import chardet                                                        #This is for running webapp on python 3.5

##DO NOT USE JSON.LOADS in this WebAPP##
##the ukko server runs python 3.5 and json.loads does not work on that version of python
##I replaced all occurances of json.loads with variableName.json()
##.json() will automatically convert the data

app.config['GOOGLE_ID'] = '58270375330-3nsdpvdmse7f652f6i8j3lkt2kaqp87p.apps.googleusercontent.com'
app.config['GOOGLE_SECRET'] = 'YF2HOss763aq2lAAbHarDe7i'
app.debug = True
app.secret_key = 'development'
oauth = OAuth(app)

# URL for the REST API
url = "http://ukko.d.umn.edu:32892"

# Initializes all of the Google authentication
# and profile information

google = oauth.remote_app(
    'google',
    consumer_key=app.config.get('GOOGLE_ID'),
    consumer_secret=app.config.get('GOOGLE_SECRET'),
    request_token_params={
        'scope': 'email profile https://www.googleapis.com/auth/calendar https://www.googleapis.com/auth/userinfo.profile'
    },
    base_url='https://www.googleapis.com/oauth2/v3/',
    request_token_url=None,
    access_token_method='POST',
    access_token_url='https://accounts.google.com/o/oauth2/token',
    authorize_url='https://accounts.google.com/o/oauth2/auth',
)

# The initial default route that gets executed (the index)

@app.route('/')
def index():
    if 'google_token' in session:
        me = google.get('userinfo')
        return redirect(url_for('home'))
    else:
        return redirect(url_for('login'))

# Makes a request to the Google API to pull user's email info
def getEmail():
    token = session.get('google_token')
    profile = requests.get("https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token="+token[0])
    profileData = profile.json()
    if 'error' in profileData:
        return redirect(url_for('login'))
    return profileData['email']

#Login method utilizing Google API
@app.route('/login')
def login():
    return google.authorize(callback=url_for('authorized', _external=True))

# Logout method utilizing Google API
@app.route('/logout')
def logout():
    session.pop('google_token', None)
    return redirect(url_for('index'))


# Verification method that confirms the login request is authentic
@app.route('/login/authorized')
def authorized():
    resp = google.authorized_response()
    if resp is None:
        return 'Access denied: reason=%s error=%s' % (
            request.args['error_reason'],
            request.args['error_description']
        )
    session['google_token'] = (resp['access_token'], '')
    me = google.get('userinfo')
    if not 'umn.edu' in getEmail():
        flash('You cannot login using this email', 'error')
        return redirect(url_for('login'))
    return redirect(url_for('home'))


# Token retrieval method using Googel API
@google.tokengetter
def get_google_oauth_token():
    return session.get('google_token')


# The homepage for UMDAlive (the index file)
@app.route('/home')

def home():
    token = session.get('google_token')
    profile = requests.get("https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token="+token[0])
    profileData = profile.json()
    if 'error' in profileData:
        return redirect(url_for('login'))
    logging.critical(getEmail())
    account = requests.get(url+"/users/"+getEmail())
    logging.critical(account)
    if account.status_code == 404:
        createUser(getEmail())

    temp = render_template(
        'index.html',
        title='Home Page',
        year=datetime.now().year,)
    return checkLogin(temp)


def createUser(email):
    token = session.get('google_token')
    profile = requests.get("https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token="+token[0])
    profileData = profile.json()
    name = profileData['given_name']+" "+profileData['family_name']

    values = {
    'name': name,
    'email': profileData['email'],
    'graduationDate':"",
    'major':"",
    'userType':"",
    'clubs': []
    }

    requests.put(url+"/createUser/",json=values)


@app.route('/joinClub/<clubName>')
def joinClub(clubName):
    updateUser(clubName,True)
    return redirect(url_for('myClubs'))


@app.route('/leaveClub/<clubName>')
def leaveClub(clubName):
    updateUser(clubName,False)
    return redirect(url_for('myClubs'))


# Add is a boolean variable used to determine if this adds or removes
def updateUser(clubName, add):
    token = session.get('google_token')
    profile = requests.get("https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token="+token[0])
    profileData = profile.json()
    name = profileData['given_name']+" "+profileData['family_name']

    account = requests.get(url+"/users/"+getEmail())
    accountData = account.json()

    users_clubs = accountData['clubs']

    if(add):
        if clubName not in users_clubs:
            users_clubs.append(clubName)
        else:
            logging.critical("Already in that club")
    else:
        if clubName in users_clubs:
            users_clubs.remove(clubName)
        else:
            logging.critical("You are not in that club.")

    logging.critical(users_clubs)

    values = {
    'name': accountData['name'],
    'email': accountData['email'],
    'graduationDate':"",
    'major':"",
    'userType':"",
    'clubs': users_clubs

    }

    requests.put(url+"/editUser/", json=values)
    # requests.delete(url+"/deleteUser/"+getEmail())
    # requests.put(url+"/userData/",json=values)


# Contact page that displays contact info
@app.route('/contact')
def contact():
    temp = render_template(
        'contact.html',
        title='Contact',
        year=datetime.now().year,
        message='This is your contact page.'
    )
    return checkLogin(temp)

# Page reserved for deleting clubs
@app.route('/delete')
def delete():
    temp = render_template(
        'delete.html',
        title='Deleting a club',
        year=datetime.now().year,
        message='This is where a club owner can delete their club.',
    )
    return checkLogin(temp)

# Helper function for delete that checks if the user making
# the request is the club owner. Returns an error if they
# are not the owner
@app.route('/deleteHelper', methods=['POST'])
def deleteHelper():
    #This login will catch an expired token and redirect to login for fresh token
    #TODO: Find a better way to refresh tokens with active login sessions
    userEmail=getEmail()
    # Checks if user is the club owner by comparing the session's
    # email with the club owner email
    clubName = request.form['search']
    searchClubsRequest = requests.get(url+"/getClub/" + clubName)
    searchClubsData = searchClubsRequest.json()

    # Checks if user is an admin

    potentialAdmin = requests.get(url + "/users/" + getEmail() ) # Gets user information
    data = potentialAdmin.json()
    role = data['userType'] # Checks if user is an admin
    logging.critical(role)

    try:
        ownerEmail = searchClubsData['ownerEmail']

        if (role == "admin"):
            data = requests.delete("http://akka.d.umn.edu:45645/deleteClub/"+clubName)
            temp = render_template(
                'delete.html',
                title='Deleting a club',
                year=datetime.now().year,
                message='This is where a club owner can delete their club.',
                found = 'Deleted ' + clubName + ' successfully.'
            )
            return checkLogin(temp)




        # Verifies if the person making the request is the owner
        if not (userEmail == ownerEmail):
            flash('You are not the owner', 'error')
            temp = render_template(
                'delete.html',
                title='Deleting a club',
                year=datetime.now().year,
                message='This is where a club owner can delete their club.',
                found = 'Sorry, You do not have access to that club'
            )
            return checkLogin(temp)
    except:
        temp = render_template(
            'delete.html',
            title='Deleting a club',
            year=datetime.now().year,
            message='This is where a club owner can delete their club.',
            found = 'No club found'
            )
        return checkLogin(temp)


    # Deletes the club using REST API
    data = requests.delete("http://akka.d.umn.edu:45645/deleteClub/"+clubName)
    temp = render_template(
        'delete.html',
        title='Deleting a club',
        year=datetime.now().year,
        message='This is where a club owner can delete their club.',
        found = 'Deleted ' + clubName + ' successfully.'
    )
    return checkLogin(temp)

# Page reserved for club creation
@app.route('/create')
def create():
    """Renders the create page."""
    temp = render_template(
        'create.html',
        title='Create',
        year=datetime.now().year,
        message='Club Creation Page.'
    )
    return checkLogin(temp)

# Helper method that verifies a club's successful creation
@app.route('/createSuccess',methods=['POST'])
def createSuccess():
    token = session.get('google_token')
    profile = requests.get("https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token="+token[0])
    profileData = profile.json()

    if 'error' in profileData:
        return redirect(url_for('login'))

    # Making a request to the API to get all clubs
    allClubsRequest = requests.get(url+"/getAllClubs/")
    allClubsData = allClubsRequest.json()
    allClubsList = allClubsData['items'] #This is a array of club lists.
    allClubsLength = len(allClubsList)

    name = profileData['given_name']+" "+profileData['family_name']
    email = profileData['email']

    values={
        'clubName':(request.form['clubName']).lower(),
        'clubOwner':name,
        'ownerEmail':email,
        'keywords':request.form['keywords'],
        'description':request.form['description']
        }

    clubAlreadyFound = False
    for n in allClubsList:
        if values['clubName'] == n:
            clubAlreadyFound = True

    if not clubAlreadyFound:
        # Create new club
        r = requests.put(url+"/creatClub/",json=values)
        temp = render_template(
        'create.html',
        title='create',
        year=datetime.now().year,
        message='Club Creation Page',
        text = 'Club creation successful.'
        )
    else:
        # Club already exists, cannot create new club
        temp = render_template(
        'create.html',
        title='create',
        year=datetime.now().year,
        message='Club Creation Page',
        text = 'Club creation unsuccessful. ' + (request.form['clubName']).lower() + ' already exists!'
        )
    return checkLogin(temp)

# Page reserved for 'the about me' section of UMDAlive
@app.route('/about')
def about():
    """Renders the about page."""
    temp = render_template(
        'about.html',
        title='About',
        year=datetime.now().year,
        message='Explore. Create. Connect'
    )
    return checkLogin(temp)

# Page reserved for pulling all of the created clubs from
# the REST API
@app.route('/allClubs')
def allClubs():
    # Making a request to the API
    allClubsRequest = requests.get(url+"/clubs/")
    allClubsData = allClubsRequest.json()
    allClubsList = allClubsData['items'] #This is a array of club lists.
    allClubsList.sort()
    allClubsLength = len(allClubsList)
    temp = render_template(
        'allClubs.html',
        title='Clubs',
        year=datetime.now().year,
        message='This is a list of all clubs at UMD.',
	    data=allClubsList,
        length=allClubsLength
        )
    return checkLogin(temp)

# Page reserved for pulling the user's Google calender
@app.route('/calendar')
def calendar():
    # This login will catch an expired token and redirect to login for fresh token
    # TODO: Find a better way to refresh tokens with active login sessions
    """Renders the Calendar page."""
    temp = render_template(
        'calendar.html',
        title='Calendar',
        year=datetime.now().year,
        message='For all clubs.'
    )
    return checkLogin(temp)


@app.route('/calendarMy')
def calendarMyClubs():
    # This login will catch an expired token and redirect to login for fresh token
    # TODO: Find a better way to refresh tokens with active login sessions
    """Renders the Calendar page."""
    temp = render_template(
        'calendarMyClubs.html',
        title='Calendar',
        year=datetime.now().year,
        message='For all my clubs.'
    )
    return checkLogin(temp)



# Helper function that pulls data necessary for calendar
@app.route('/data')
def return_data():
    r = requests.get(url+"/posts/")
    data = r.json()
    data_set = json.dumps(data["items"])
    temp =""
    temp += data_set
    tempNew = temp.replace("[","")


    logging.critical(tempNew)

    start_date = request.args.get('start', '')
    end_date = request.args.get('end', '')
    # You'd normally use the variables above to limit the data returned
    # you don't want to return ALL events like in this code
    # but since no db or any real storage is implemented I'm just
    # returning data from a text file that contains json elements
    input_data = '{"title":"Meeting","start":"2014-09-12"}'


    # you should use something else here than just plaintext
    # check out jsonfiy method or the built in json module
    # http://flask.pocoo.org/docs/0.10/api/#module-flask.json
    return temp

# Helper function that pulls data necessary for calendar
@app.route('/dataMyClubs')
def return_dataMyClubs():
    myClubsRequest = requests.get(url+"/users/"+str(getEmail()))
    myClubsData = myClubsRequest.json()
    myClubsList = myClubsData['clubs'] # Array of club lists.
    myClubsLength = len(myClubsList)
    temp =""
    for clubs in myClubsList:
        r = requests.get(url+"/posts/"+clubs)
        data = r.json()
        data_set = json.dumps(data["items"])
        tempData = data_set
        tempData = tempData.replace("[","").replace("]","")
        temp += tempData

    temp = temp.replace("}{","}, {")
    temp = "["+temp+"]"



    logging.critical(temp)

    start_date = request.args.get('start', '')
    end_date = request.args.get('end', '')
    # You'd normally use the variables above to limit the data returned
    # you don't want to return ALL events like in this code
    # but since no db or any real storage is implemented I'm just
    # returning data from a text file that contains json elements
    input_data = '{"title":"Meeting","start":"2014-09-12"}'


    # you should use something else here than just plaintext
    # check out jsonfiy method or the built in json module
    # http://flask.pocoo.org/docs/0.10/api/#module-flask.json
    return temp



# Page reserved for pulling all of the clubs the user is
# signed up for
@app.route('/myClubs')
def myClubs():
    myClubsRequest = requests.get(url+"/users/"+str(getEmail()))
    myClubsData = myClubsRequest.json()
    myClubsList = myClubsData['clubs'] # Array of club lists.
    myClubsList.sort()
    myClubsLength = len(myClubsList)
    temp = render_template(
        'myClubs.html',
        title='Clubs I\'m following',
        year=datetime.now().year,
        message='This is a list of my clubs at UMD.',
	    data=myClubsList,
        length=myClubsLength
        )
    return checkLogin(temp)

# Page reserved for making posts for the user's club
@app.route('/postForYourClubs')
def postForYourClubs():
    email = getEmail()
    myClubsRequest = requests.get(url+"/clubs/email/"+email)
    myClubsData = myClubsRequest.json()
    myClubsList = myClubsData['items'] # Array of club lists.
    myClubsLength = len(myClubsList)
    temp = render_template(
        'postForYourClubs.html',
        title='Clubs',
        year=datetime.now().year,
        message='This is a list of my clubs at UMD.',
	    data=myClubsList,
        length=myClubsLength
        )
    return checkLogin(temp)

# Page reserved for deleting posts
@app.route('/postDeletion/')
def postDeletion():

    var = "Test"

    myDeleteRequest = requests.delete(url+'/deletePost/'+var) #delete a post
    #postDeleted = myDeleteRequest.json()

    temp = render_template(
        'delete.html',
        title='Deleting a club',
        year=datetime.now().year,
        message='This is where a club owner can delete their club.',
        #found = 'Deleted ' + clubName + ' successfully.'
        )
    return temp

# Helper function for user club posting
@app.route('/postHelper/<clubName>')
def postHelper(clubName):
    temp = str(clubName)
    userEmail = getEmail()
    searchClubsRequest = requests.get(url+"/clubs/" + clubName)
    searchClubsData = searchClubsRequest.json()
    ownerEmail = searchClubsData['ownerEmail']
    # Verifies the user has permission to delete the club
    if not (userEmail == ownerEmail):
        return redirect(url_for('postForYourClubs'))
    temp = render_template(
        'postHelper.html',
        title='Post for '+ clubName+ ' Club',
        year=datetime.now().year,
        message='Your application description page.',
        club = clubName
    )
    return checkLogin(temp)

# Submit function used by post helper for submitting posts
@app.route('/postForYourClubsHelper/submit/<clubName>',methods=['POST'])
def postHelperSubmit(clubName):

    date = request.form['date']+"T"+request.form['time'];

    values={
        'clubName':clubName,
        'title':request.form['title'],
        'date':date,
        'location':request.form['location'],
        'description':request.form['description'],
        'image':None
        }

    r = requests.put(url+"/posts/",json=values)

    return redirect(url_for('postForYourClubs'))

# Page reserved for displaying the user's profile
@app.route('/profile')
def profile():
    token = session.get('google_token')
    profile = requests.get("https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token="+token[0])
    profileData = profile.json()
    if 'error' in profileData:
        return redirect(url_for('login'))
    email = profileData['email']
    name = profileData['name']
    picture = profileData['picture']

    temp = render_template(
        'profile.html',
        title='Profile',
        year=datetime.now().year,
        message='This is the profile page.',
        name=name,
        email=email,
        picture=picture
        )
    return checkLogin(temp)

# Page reserved for searching for a club based on club name
@app.route('/searchClubs', methods=['GET'])
def searchClubs():
    temp = render_template(
        'searchClubs.html',
        title='Searching Clubs',
        year=datetime.now().year,
        message='This is where you can search for clubs.'
        )
    return checkLogin(temp)

# Helper function that checks if a club exists
# in the database
@app.route('/found', methods=['POST'])
def found():
    clubName = request.form['search']
    if not (clubName == ""):
        searchClubsRequest = requests.get(url+"/clubs/" + clubName)
        searchClubsData = searchClubsRequest.json()
        # Attempts to access club
        try:
            error = searchClubsData['message']
        # If an error is found, the club was found and a a club profile page will be generated
        except:
            temp = str(clubName)
            clubProfileRequest = requests.get(url+"/clubs/"+temp)
            clubProfileData = clubProfileRequest.json()
            name = "Welcome to the " + clubProfileData['clubName'] + " club"
            owner = "Club Owner: " + clubProfileData['clubOwner']
            email = clubProfileData['clubOwner'] + "'s E-Mail: " + clubProfileData['ownerEmail']
            kword = "Keywords: " + clubProfileData['keywords']
            desc = "Description: " + clubProfileData['description']
            temp = render_template(
                'clubProfile.html',
                title=name,
                clubOwner =owner,
                ownerEmail = email,
                description = desc,
                keywords = kword,
                cname = request.form['search']
            )
            return checkLogin(temp)

        search = request.form['search']
        # Making a request to the API
        clubNameList = ""
        keywordList = ""
        clubNameLength=0
        keywordLength=0
        try:
            clubNameRequest = requests.get(url+"/clubs/"+search)
            clubNameData = clubNameRequest.json()
            clubNameList = clubNameData['clubName'] #This is a array of club listS.
            clubNameLength = len(clubNameList)
        except:
            keywordRequest = requests.get(url+"/clubSearch/"+search)
            keywordData = keywordRequest.json()
            keywordList = keywordData['items'] #This is a array of club lists.
            keywordList.sort()
            keywordLength = len(keywordList)

    else:
        keywordLength=0


    if(keywordLength > 0):
        temp = render_template(
            'clubResults.html',
            title='Results',
            year=datetime.now().year,
            data=clubNameList,
            keyworddata=keywordList,
            length=clubNameLength,
            keywordLength=keywordLength
            )
        return checkLogin(temp)
    else:
    # This means the club was not found
        temp = render_template(
            'searchClubs.html',
            title='Searching Clubs',
            year=datetime.now().year,
            message='This is where you can search for clubs.',
            found='No Club Found'
            )
        return checkLogin(temp)


# Page reserved for sending email to the developers
@app.route('/send')
def send():
    """Renders the Send page."""
    subTitle = 'Want to stay in the loop? '
    subTitle += 'Have questions or concerns? Contact us!'
    temp = render_template(
        'send.html',
        title='Send',
        year=datetime.now().year,
        message=subTitle
        )
    return checkLogin(temp)

# Page reserved for sharing information
@app.route('/share')
def share():
    """Renders the Share page."""
    subTitle = 'Want to stay in the loop? '
    subTitle += 'Have questions or concerns? Contact us!'
    temp = render_template(
        'share.html',
        title= 'Share',
        year= datetime.now().year,
        message= subTitle
        )
    return checkLogin(temp)

# Page reserved for generating a club profile page
@app.route('/clubProfile/<clubName>')
def clubProfile(clubName):
    """Renders page"""
    temp = str(clubName)
    clubProfileRequest = requests.get(url+"/clubs/"+temp)
    clubProfileData = json.loads(clubProfileRequest.content)
    posts = postsGetter(temp)
    if(temp[0:3].lower()=="the"):
        temp = temp[4:len(temp)]
    flag = False
    for x in range (0,len(temp)-3):
        if(temp[x:x+4].lower()=="club"):
            flag = True
            break
    if(flag != True):
        temp+=' club'
    name = "Welcome to the " + temp
    owner = "Club Owner: " + clubProfileData['clubOwner']
    email = clubProfileData['clubOwner'] + "'s E-Mail: " + clubProfileData['ownerEmail']
    kword = "Keywords: " + clubProfileData['keywords']
    desc = "Description: " + clubProfileData['description']
   # posts = posts['items'] #Collection of posts in a array inside of a hashmap
    temp = render_template(
        'clubProfile.html',
        title=name,
        clubOwner =owner,
        ownerEmail = email,
        description = desc,
        keywords = kword,
        data = posts,
        cname = clubName
        )
    return checkLogin(temp)

# Helper function that gets a club's posts
def postsGetter(clubName):
    postsRequest = requests.get(url+"/posts/"+clubName)
    postsArray = [x for x in postsRequest.json()['items']]
#    postsData = json.loads(postsRequest.content)
    return postsArray

#Redirects user to login page if not logged in.
def checkLogin(template):
    if not ('google_token' in session):
        return redirect(url_for('login'))
    else:
        return template




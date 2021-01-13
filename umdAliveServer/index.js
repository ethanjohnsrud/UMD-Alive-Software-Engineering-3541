//start server npm run bootServer
//check processes ps aux \ grep userName
//to whoever gets this code next, good luck.
//cheek010@d.umn.edu

var express = require('express');
var queryParser = require('body-parser');

// Initialize main instanced class
var app = express();

// Set the port
app.set("port", 22643);

// Support encoded bodies
app.use(queryParser.urlencoded({
    extended: true
}));

// Support JSON-encoded bodies
app.use(queryParser.json());

//loads the data base functions
var dataBase = require('./dataBaseFunctions.js');

//Club server Calls
app.put('/createClub', function (req, res) {
  if (!req.body){
    return res.sendStatus(400);
  }
  var clubData = {
    "name" : req.body.name,
    "description" : req.body.description,
    "members" : req.body.members,
    "events" : req.body.events,
    "profilePic" : req.body.profilePic
  };
  dataBase.createClub(clubData, function (doc){
    res.send(doc);
  });
});

app.put('/editClub', function (req, res){
  if (!req.body){
    return res.sendStatus(400);
  }
  var clubData = {
    "name" : req.body.name,
    "description" : req.body.description,
    "members" : req.body.members,
    "events" : req.body.events,
    "profilePic" : req.body.profilePic
  };
  dataBase.editClub(req.body._id, clubData);
  res.send({});
});

app.get('/getClub/:clubID', function (req, res) {
  dataBase.getClub(req.params.clubID, function (docs) {
    res.send(docs);
  });
});

app.get('/getAllClubs', function (req, res) {
  dataBase.getAllClubs(function (docs){
    res.send(docs);
  });
});

app.put('/joinClub', function (req, res){
  if (!req.body){
    return res.sendStatus(400);
  }
  dataBase.joinClub(req.body.userID, req.body.clubID);
  res.send({});

});

app.put('/leaveClub', function (req, res){
  if (!req.body){
    return res.sendStatus(400);
  }
  dataBase.leaveClub(req.body.userID, req.body.clubID);
  res.send({});
});

app.delete('/deleteClub/:clubID', function (req, res){
  dataBase.deleteClub(req.params.clubID);
  res.send({});
});

//User server Calls
app.put('/createUser', function (req, res){
  if (!req.body){
    return res.sendStatus(400);
  }
  console.log(req.body);
  var userData = {
    "name" : req.body.name,
    "email" : req.body.email,
    "major" : req.body.major,
    "description" : req.body.description,
    "clubs" : req.body.clubs,
    "userID" : req.body.userID,
    "profilePic" : req.body.profilePic
  };
  dataBase.createUser(userData);
  res.send({});
});

app.put('/editUser', function (req, res){
  if (!req.body){
    return res.sendStatus(400);
  }
  var userData = {
    "name" : req.body.name,
    "email" : req.body.email,
    "major" : req.body.major,
    "description" : req.body.description,
    "clubs" : req.body.clubs,
    "userID" : req.body.userID,
    "profilePic" : req.body.profilePic
  };
  dataBase.editUser(req.body.userID, userData);
  res.send({});
});

app.get('/getUser/:userID', function (req, res){
    dataBase.getUser(req.params.userID, function (docs){
      res.send(docs);
    });
});

app.get('/users/:email', function (req, res) {
    var user;
        console.log("Looking for " + req.params.email);

    mongodb.findUser(req.params.email, function(result){

        if(result.length > 0){
                        var user = result[0];
                        console.log(user);
                        console.log("Found user.");
                        //console.log(res.body);
            //            res.query = JSON.stringify(user.userData);//was user.userData
            //                        console.log("-------------------")
            //                                    res.send(res.query);
            //
        } else {
                        res.send(404);

        }

    });
    //    res.send(JSON.stringify(dummyUser1));
    //
});

app.put('/userData', function (req, res) {
        // If for some reason the JSON isn't parsed, return HTTP error 400
    if (!req.body) return res.sendStatus(400);

    var userData = {
        name: req.body.name,
        major: req.body.major,
        email: req.body.email,
        graduation_date: req.body.graduationDate,
        userType: req.body.userType,
        users_clubs: [],
    };

    mongodb.insertUser(userData);
    console.log("Creating user" + req.body.name);

    var jsonResponse = {
        id: '123', status: 'updated'
    };
    res.json(jsonResponse);
    //res.json(userData);
});

//Event server calls
app.put('/createEvent', function (req, res){
  if (!req.body){
    return res.sendStatus(400);
  }
  var eventData = {
    "name" : req.body.name,
    "description" : req.body.description,
    "date" : req.body.date,
    "time" : req.body.time,
    "club" : req.body.club
  };
  dataBase.createEvent(eventData, function(doc){
		console.log(doc);
		res.send(doc);
  });
});

app.put('/editEvent', function (req, res){
  if (!req.body){
    return res.sendStatus(400);
  }

  var eventData = {
    "name" : req.body.name,
    "description" : req.body.description,
    "date" : req.body.date,
    "time" : req.body.time,
    "club" : req.body.club
  };

  dataBase.editEvent(req.body._id, eventData);
  res.send({});
});

app.get('/getEvent/:eventID', function (req, res) { 
  console.log("getEvent");
  dataBase.getEvent(req.params.eventID, function (docs) {
	console.log(docs);
    res.send(docs);
  });
});

app.get('/getAllEvents', function (req, res){
  dataBase.getAllEvents(function (docs){
    res.send(docs);
  });
});

app.delete('/deleteEvent/:eventID', function (req, res){
  dataBase.deleteClub(req.params.eventID);
  res.send({});
});

////////// SECOND TEAM STUFF

/*///////////////////////////
 *   End of dummy users/clubs
 *////////////////////////////

/*
 ***********************
 * DELETE ROUTE SECTION
 ***********************
 */

app.delete("/delete", function(req, res){
    if (!req.body)
        return res.sendStatus(400);

    console.log("GIMME DATA" + req.body.clubName);	
    mongodb.delete(req.body.clubName);

    
    console.log("Club has been deleted: " + req.body.clubName);

    res.sendStatus(200);
}
);

app.delete("/delete/:clubName", function(req, res){
    
    console.log("GIMME DATA" + req.body.clubName);	
    mongodb.delete(req.params.clubName);

    
    console.log("Club has been deleted: " + req.body.clubName);

    res.sendStatus(200);
}
);

app.delete("/deleteUser", function(req, res){
	if (!req.body)
		return res.sendStatus(400);

	mongodb.deleteUser(req.body.userName);

	res.sendStatus(200);

	console.log("User has been deleted: " + req.body.userName);
}
);

app.delete("/deletePost", function(req, res){
	if (!req.body)
		return res.sendStatus(400);

	mongodb.deletePost(req.body.title);
	
	res.sendStatus(200);

	console.log("Post has been deleted: " + req.body.title);
	
}
);

/*
 ************************
 * PUT ROUTE SECTION
 ************************
 */
app.put('/clubs', function (req, res) {

    // If for some reason the JSON isn't parsed, return HTTP error 400
    if (!req.body)
        return res.sendStatus(400);
	
    //console.log(req.body);	
    // Takes data from request and makes a new object
    var clubData = {
        clubName: req.body.clubName,
        clubOwner: req.body.clubOwner,
	ownerEmail : req.body.ownerEmail,
        keywords: req.body.keywords,
        description: req.body.description
    };

	//console.log(clubData);

    mongodb.insertClub(clubData);

    var jsonResponse = {
        id: '123', status: 'updated'
    };
    res.json(jsonResponse);

    console.log("New club has been created: " + req.body.clubName);
});

app.put('/posts', function (req, res) {
    // If for some reason the JSON isn't parsed, return HTTP error 400
    if (!req.body) return res.sendStatus(400);

    var postData = {
        clubName: req.body.clubName,
        title: req.body.title,
        time: req.body.time,
        date: req.body.date,
        location: req.body.location,
        description: req.body.description,
        image: req.body.image
    };

    mongodb.insertPost(postData);

    console.log("Club posting: " + req.body.clubName);
    console.log("Title of post: " + req.body.title);
    var jsonResponse = {
        id: '123', status: 'updated'
    };
    res.json(jsonResponse);
});

app.put('/userData', function (req, res) {
    // If for some reason the JSON isn't parsed, return HTTP error 400
    if (!req.body) return res.sendStatus(400);

    var userData = {
        name: req.body.name,
        major: req.body.major,
        email: req.body.email,
        graduation_date: req.body.graduationDate,
        userType: req.body.userType,
        users_clubs: [],
    };

    mongodb.insertUser(userData);
    console.log("Creating user" + req.body.name);

    var jsonResponse = {
        id: '123', status: 'updated'
    };
    res.json(jsonResponse);
    //res.json(userData);
});

/*
 ************************
 * GET ROUTE SECTION
 ************************
 */

app.get('/clubs', function (req, res) {
    //array to which each club will be stored
    var clubNames = {
        items: []
    };
    mongodb.getCollection('clubs', function(result){
            var clubsData = {
                jsonArray: []
            };

            result.forEach(function(clubs){
                clubsData.jsonArray.push(clubs);
            });

            for(var i = 0; i < clubsData.jsonArray.length; i++){
                var curClub = clubsData.jsonArray[i];
                clubNames.items[i] = curClub.clubData.clubName;
            }

            var stringArray = JSON.stringify(clubNames);
            console.log("clubs being sent to client: " + stringArray);
            res.send(stringArray);
    });

});

app.get('/clubs/email/:email', function (req, res) {
    //array to which each club will be stored
    var clubNames = {
        items: []
    };
    mongodb.getCollection('clubs', function(result){
            var clubsData = {
                jsonArray: []
            };

            result.forEach(function(clubs){
                clubsData.jsonArray.push(clubs);
            });
            count = 0;
            for(var i = 0; i < clubsData.jsonArray.length; i++){
                var curClub = clubsData.jsonArray[i];
                if(curClub.clubData.ownerEmail==req.params.email){
                    clubNames.items[count] = curClub.clubData.clubName;
                    console.log("Made it in");
		    count++;
                }
            }

            var stringArray = JSON.stringify(clubNames);
            console.log("clubs being sent to client: " + stringArray);
            res.send(stringArray);
    });

});

app.get('/clubs/:clubName', function (req,res) {
    var club;
    console.log("Looking for " + req.params.clubName);

    mongodb.findClub(req.params.clubName, function(result){
        var club = result[0];
	if(club) {
	        console.log("Found club.");
	        res.query = JSON.stringify(club.clubData);
	        res.send(res.query);
	}
	else {
		var clubNotFoundMessage = "Club " + req.params.clubName + " not found";
		res.send({"message" : clubNotFoundMessage});
	}
    });
});

app.get('/clubSearch/:keyword', function (req,res) {
    //array to which each club will be stored
    var clubNames = {
        items: []
    };

    mongodb.getCollection('clubs', function(result){
            var clubsData = {
                jsonArray: []
            };

            result.forEach(function(clubs){
                clubsData.jsonArray.push(clubs);
            });

            var x = 0;
            for(var i = 0; i < clubsData.jsonArray.length; i++){
                var curClub = clubsData.jsonArray[i];
                if(req.params.keyword === curClub.clubData.keywords) {
                    clubNames.items[x] = curClub.clubData.clubName;
                    x++;
                    }
            }

            var stringArray = JSON.stringify(clubNames);
            console.log("clubs being sent to client: " + stringArray);
            res.send(stringArray);
    });
});
//should return userEmail
//Only returns dummy
app.get('/users/:email', function (req, res) {
var user;
    console.log("Looking for " + req.params.email);

    mongodb.findUser(req.params.email, function(result){
    
        if(result.length > 0){
            var user = result[0];
            console.log(user);
            console.log("Found user.");
            //console.log(res.body);
            res.query = JSON.stringify(user.userData);//was user.userData
            console.log("-------------------")
            res.send(res.query);
        } else {
            res.send(404);
        }
    });
//    res.send(JSON.stringify(dummyUser1));
});

//returns an array of all users
//Only returns dummy
app.get('/userData/', function (req, res) {
 //array to which each club will be stored
    var userEmails = {
        userItems: []
    };
    mongodb.getCollection('users', function(result){
            var usersData = {
                jsonArray: []
            };

            result.forEach(function(users){
                usersData.jsonArray.push(users);
            });


            for(var i = 0; i < usersData.jsonArray.length; i++){
                var curUser = usersData.jsonArray[i];

                userEmails.userItems[i] = curUser.userData.email;
            }

            var stringArray = JSON.stringify(userEmails);

            console.log("users being sent to client: " + stringArray);
            res.send(stringArray);

    });


});

app.get('/posts', function (req, res) {
    var mostRecentPosts = {
        items: []
    };

    mongodb.getCollection('posts', function(result){
                var postsData = {
                    jsonArray: []
                };

                result.forEach(function(posts){
                    postsData.jsonArray.push(posts);
                });

                for(var i = 0; i < postsData.jsonArray.length; i++){
                    var curPost = postsData.jsonArray[i];
                    mostRecentPosts.items[i] = curPost.postData;
                }

                var stringArray = JSON.stringify(mostRecentPosts);
                res.send(stringArray);
    });
});

app.get('/posts/:clubName', function (req, res) {
    var mostRecentPosts = {
        items: []
    };

    mongodb.getCollection('posts', function(result){
                var postsData = {
                    jsonArray: []
                };

                result.forEach(function(posts){
                    postsData.jsonArray.push(posts);
                });

                for(var i = 0; i < postsData.jsonArray.length; i++){
                    var curPost = postsData.jsonArray[i];
                    console.log(curPost.postData.clubName);
                    if(curPost.postData.clubName == req.params.clubName){
                        console.log("Made it in the if");
                        mostRecentPosts.items[i] = curPost.postData;
                    }

                }

                var stringArray = JSON.stringify(mostRecentPosts);
                res.send(stringArray);
    });
});




function getClubKeyword(position) {
    return clubs.items[position].keywords;
}
//Listen
app.listen(app.get("port"), function () {
    console.log('umdAliveServer running on port ', app.get("port"));
});

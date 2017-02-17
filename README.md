Information:
	This program consisests of two parts.
	The first part will download Github repositories and their most recent commit patch files.
	The second part will analyze the the repositorys and patches using SciTool's Understand
	
	My abstraction of the program was to parse through the JSON payload from Github and grab multiple repositories.
	I would use these to then access the the commits using a constructed URL with oAuth.
	The patch file would then be compared to the UDB's dependency call graph which was constructed by Understand.
	It will output a text file with all the nodes that should be tested.
	

How to run:
	Requirements:
		Scala
		Simple Build Tools

	Step 0:
		You must first add SciTool Understand's jar file to your libraries dependencies manually from the local path.
		You must then add that dependency to both the modules in the project.
		
	Step 1:
		Go to the top menu and go to Run > Run...
			Select downloadProjects
			Enter the programming language which will tell the program which github projects to download
				(It defaults to 5 projects, if you would like the change the amount the variable controlling it (downloads) is on line 21 of downloadProjects.scala)
			Wait for it to finish, it may take some time.
	Step 2:
		Go to the top menu and go to Run > Run...
			Select analyzeProjects
			Enter the language you chose in the first step, and provide the most common exstension of that language's source files.
			Ex. 
					Please enter language: 
					java
					Please enter one language extension type (ex. .cpp): 
					.java
			Wait for it to finish.
	Step 3:
		Go to the projects local directory and open the file called nodesToCheck.txt
			This fill will contain all the nodes that call the nodes that changed in the previous commit.
			
			
	* You should manually delete the downloads folder inside the project BEFORE using the downloader again, or it may cause issues
	


Akka Actors:
	My akka actor is the downloader part. The reciever has the code to download .patch files and GitHub repositories.
	I end the actor from inside itself at the end of its run.
	The actor is iniated at the bottom of the code file and sends a message to the reciever with the case class to download the projects.

Limitations:
	It requires the user to input the project's source type
			
Tests:
	To run the tests:
	Go to the top menu and go to Run > Run...
			Select sTest
			
			
* If there are errors for the UDB command line calls, please make sure there are NO spaces in the absolute path to the project folder.
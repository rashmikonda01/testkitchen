To begin, move the db.properties.tmp file to db.properties
Replace the  user/password with whatever you wish
To create a new project, run "ant -Dproject=myProject create-project"
In your database, you may need to run "create database myProject"
Create any necessary sql deltas in the project/myProject/deltas folder
If you wish to compile and run your test cases, run "ant -Dproject=myProject env=test" This will run your deltas beforehand
To run just the deltas, use "ant -Dproject=myProject env=test default"
If you wish to test more than one project, run "ant -Denv=test -Dprojects=projectA,projectB,projectC"


If you encounter errors running a transformation, it may be missing libraries and will need to be added to ivy.xml
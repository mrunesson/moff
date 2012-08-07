Moff mirrors a Maven artifact and all its dependencies, including source and plugins.


Usage:
java -jar moff.jar groupId artifactId version

An Maven repository is created in the moff subdirectory to the current
directory. The directory moff is created if missed. 
Moff will not try to download a Maven artifact if it already exist in the 
moff-repository.




Known issues:
* If Moff fails to download an artifact it will crash. You may end up with
  artifacts were all dependencies are downloaded correct.


TODO:
* Make tests work and create more tests.
* Customized destination directory
* Support for list of artifacts to download.



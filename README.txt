/*
* Scheduler Export Java Demo
* Bryntum AB copyrights 2012/2013
*/

This demo utilizes the Export plugin for generating a PDF/PNG file out of our components with Java, Spring MVC and Hibernate 3.5 frameworks. 
Unfortunately because this process is rather complicated, there is no way of running it 
completely on the client side (in the browser). 

For this demo to work there are some requirements :

- Java JDK 1.6 installed and added to the system path
- Java web server (preferably Apache Tomcat)
- Eclipse IDE JEE version
- PhantomJs (http://phantomjs.org) in version 1.6+ installed on your system, and added 
  to your environment PATH (so that it's runnable from the console/terminal).
  Provide server user with rights to run it. Instead of inserting it into PATH you can 
  also update the phantomPath variable in "ExportController.java" with appropriate value.  
- ImageMagick (http://www.imagemagick.org) installed on your system, and runnable 
  from the console/terminal. Also check the rights to run it and in the same way
  as phantom, either insert in PATH or provide correct value for imgkPath in "ExportController.java".
  For Windows based systems please consult steps 1-3 of this instruction on installing ImageMagick with PHP support : 
  http://elxsy.com/2009/07/installing-imagemagick-on-windows-and-using-with-php-imagick/ (most important point is the path to image magick).
- Provide your server user with the rights for CRUD operations on files in the example folder. 
- Server script as well as temporary files should reside in the example folder because 
  relative links to css files are used.

The process of setting it up consists of the following steps :

1. Make sure the required components are installed
2. Add new server to servers in Eclipse
3. Import the example project to Eclipse and add `WebContent/WEB-INF` folder to build sources
4. Run the project on the previously created server
5. Go to `localhost:8080/Java Export` in a browser to see your application running

After our environment is ready, we will focus on the `ExportController.java` file. Here at the beginning 
of the script you'll find some important variables that need explaining:

    String outName = range + "-exportedPanel" + new Timestamp(new java.util.Date().getTime()).toString().replaceAll(" ", "") + "." + fileFormat;
    String outputPath = request.getSession().getServletContext().getRealPath("/");  
    String imgkPath = "";
    String phantomPath = "phantomjs";

The first `outName` variable defines the name of the exported file. Because many users of your 
application may want to print at the same time - a timestamp is added. `outputPath` is the path 
to our example, where all files should be located. Changing this may lead to unexpected behavior 
of the demo. `imgkPath` is the path to the ImageMagick folder, which can remain blank if its folder is in the PATH variable.

The last interesting part of the code is the command for the ImageMagick when exporting to pdf:

    cmd.add(imgkPath+"convert");

Depending on the installed version, adding '-density' parameter might be required to prevent program 
from dropping the quality of the exported image. The value of the parameter may be different across 
OS, but '-density 30' was tested to work in most cases. 

and when exporting to png :

    filesString = imgkPath+"montage -mode concatenate -tile 1x";

The current setting is tested and works well on most systems. For more details about the 'montage' command please consult
the link http://www.imagemagick.org/Usage/montage/. For details regarding commands of ImageMagick see http://www.imagemagick.org/script/command-line-options.php.

At this moment we should have our example up and running.
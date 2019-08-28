
Fast and reliable open source CDN written in Scala / Akka 

###Configuration
Configuration takes place in `application.json` in the resource folder. It can obviously be overridden at your convenience  
`port` => Http port of the service  
`baseFolder`  => Folder containing the source images  
`cacheFolder` => Folder where cached version of the images with be stored


##What does it do ?

This very simple service will resize and cache image on the fly.
Example: in your `baseFolder` you have /images/subfolder/a.jpg, you can access it with http://service:port/images/subfolder/a.jpg. Nothing crazy there, Nginx does it better.

If you access http://service:port/images/subfolder/a.jpg?width=400, the service will resize and serve it on the fly and will store the generated thumbnail in the `cacheFolder` directory.



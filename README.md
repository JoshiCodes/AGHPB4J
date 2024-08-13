<div align="center">

# ☕ AGHPB4J 📚
<sub>Java Wrapper for the anime girls holding programming books [API](https://api.devgoldy.xyz/aghpb/v1/docs).

![GitHub Release](https://img.shields.io/github/v/release/JoshiCodes/AGHPB4J?include_prereleases&sort=date&display_name=release)
<img src="https://repo.joshicodes.de/api/badge/latest/releases/de/joshicodes/AGHPB4J?prefix=v">


</div>

<div align="center">
    <img src="https://github.com/cat-milk/Anime-Girls-Holding-Programming-Books/blob/master/Java/Unnamed_Core_Java.jpg?raw=true" width="500px">
</div>

> [!Note]
> 
> I am not the original author of the AGHPB API. You can view their Wrappers for other Languages [here](https://github.com/THEGOLDENPRO/aghpb_api#-api-wrappers).

This Wrapper is based on the [AGHPB API](https://github.com/THEGOLDENPRO/aghpb_api/). It is a simple Java Wrapper that allows you to interact with the API in a more convenient way.

## Installation
<img src="https://repo.joshicodes.de/api/badge/latest/releases/de/joshicodes/AGHPB4J?prefix=v&name=Version">

To use this Wrapper, you need to add the following repository and dependency to your `pom.xml` file.
Replace `VERSION` with the latest version seen above or found [here](https://github.com/JoshiCodes/AGHPB4J/releases).

```xml
<repositories>
    <repository>
        <id>joshicodes-de-releases</id>
        <name>JoshiCodes Repository</name>
        <url>https://repo.joshicodes.de/releases</url>
    </repository>
</repositories>
```

```xml
<dependencies>
    <dependency>
        <groupId>de.joshicodes</groupId>
        <artifactId>AGHPB4J</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```

## Usage
To use the API, you first need to create a new AGHPB object. You can do this by calling the constructor and passing the URL of the API as a parameter. 

```java
AGHPB aghpb = new AGHPB("http://localhost:5000");
// If no url is provided, the default public api will be used (https://api.devgoldy.xyz/aghpb/v1/)
AGHPB aghpb = new AGHPB();
```

After creating the object, just use one of the available methods.
Almost every method, returns a RestAction Object. To execute the request, you need to call the `#execute()` method.
This method is blocking and will return the result. 
If you want to execute the request asynchronously, you can use the `#queue()` method.
This method will return `void`, but can take a `Consumer` as a parameter, which will be called when the request is completed.

```java
AGHPB aghpb = new AGHPB();


AGHPBook book = aghpb.retrieveRandomImage().execute(); // Blocking
System.out.println(book.getUrl());
// or
aghpb.retrieveRandomImage().queue(book -> System.out.println(book.getUrl())); // Asynchronous
```

### For more examples, see [here](https://github.com/JoshiCodes/AGHPB4J/blob/master/examples/Examples.java).

#### Methods
- `AGHPB#retrieveStatus()` - `ApiStatus`
    Returns the status of the API.
- `AGHPB#retrieveInfo()` - `ApiInfo`
    Returns the info of the API.
- `AGHPB#retrieveAllCategories()` - `List<String>`
    Returns a list of all available categories.
- `AGHPB#retrieveRandomImage(@Nullable String category, @Nullable AGHPBook.BookImageType type)` - `AGHPBook`
    Returns a random image. Both parameters are optional and alternatively you can use the `AGHPB#retrieveRandomImage()` method without parameters or with only one parameter.
- `AGHPB#retrieveSearch(String query)` - `List<AGHPBook>`
    Returns a list of images that match the search query. Alternatively you can use `AGHPB#retrieveSearch(String query, int limit)`, `AGHPB#retrieveSearch(String query, int limit, int page)` or `AGHPB#retrieveSearch(String query, String category)` or `AGHPB#retrieveSearch(String query, String category, int limit)`.
- `AGHPB#retrieveBook(int searchId)` - `AGHPBook`
    Returns a book by its search id. Alternatively you can provide a BookImageType to specify the type of the image.
    Instead of the searchId, you can also provide an unfinished AGHPBook object, to retrieve its image.

<br>

You can find the full JavaDocs [here](https://repo.joshicodes.de/javadoc/releases/de/joshicodes/AGHPB4J/latest/).

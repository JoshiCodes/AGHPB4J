<div align="center">

# ☕ AGHPB4J 📚
<sub>Java Wrapper for the anime girls holding programming books [API](https://api.devgoldy.xyz/aghpb/v1/docs).

</div>

<div align="center">
    <img src="https://raw.githubusercontent.com/cat-milk/Anime-Girls-Holding-Programming-Books/master/Java/Natsukawa_Masuzu_Java_Programming.png" width="400px">
</div>

> [!Note]
> 
> I am not the original author of the aghpb API. You can view their Wrappers for other Languages [here](https://github.com/THEGOLDENPRO/aghpb_api#-api-wrappers).

This Wrapper is based on the [AGHPB API](https://github.com/THEGOLDENPRO/aghpb_api/). It is a simple Java Wrapper that allows you to interact with the API in a more convenient way.

## Installation
// TODO

## Usage
To use the API, you first need to create a new AGHPB object. You can do this by calling the constructor and passing the URL of the API as a parameter. 

```java
AGHPB aghpb = new AGHPB("http://localhost:5000");
// If no url is provided, the default public api will be used (https://api.devgoldy.xyz/aghpb/v1/)
AGHPB aghpb = new AGHPB();
```
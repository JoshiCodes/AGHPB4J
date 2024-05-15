<div align="center">

# Simple Java Wrapper for the AGHPB API 

</div>

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
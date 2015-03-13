# swagger-definition-generator
Generates the definition part of a swagger 2.0 spec from a JSON response

Saves the tediousness of all the setup by taking a guess at what the swagger definition spec might look like by looking at just one response.

---- Known Hacks ----

Supports only basic types number, integer, boolean or string (default), objects and arrays.
Only the first element of an array is analysed, all other elements in the array are assumed to be the same.

# Yelp Search

## *Qiwen Wang*

**Yelp search** displays a list of search results from the Yelp API, displays the results in a scrollable list, and display business details. 

Time spent: **12** hours spent in total

## Instruction
To run the project, add your API key in your local `gradle.properties` file.
```
API_KEY="YourSecretAPIKey"
```

## Functionality 

The following **required** functionality is completed:

* [X] Ability to query the Yelp API to get results from a search query
* [X] The search results are displayed in a RecyclerView

The following **extensions** are implemented:

* [X] User sees a descriptive error message if internet is not available
* [X] Allow user to refresh the page. It's useful when the internet is re-enabled.
* [X] Query search result by "Hot & New", "Open now", price and location.
* [X] Set the default location as the location of the user
* [X] Display Yelp-branded stars and logo
* [X] Add a search component in the action bar
* [X] Implement infinite scrolling by the offset
* [X] Add a new activity that shows business details including additional information like open hours, full address, phone number, photo highlights, and a link to the yelp website.
* [X] Avoid exposing the API key

## Video Walkthrough

Here's a walkthrough of implemented user stories:

### Network connection and refresh
- If the network is not available, the app provides info in a snackbar.
- If the network is available again, the user can swipe down to refresh the page.
<img src='https://imgur.com/download/JgejiFU' title='Network' width='' alt='Video Walkthrough' />

### Search page
- User can search the restaurants in the search menu.
- User can sort by "Recommended", "Distance", "Rating", and "Most reviewed".
- Other search filters:
  - Hot & New
  - Open Now
  - Price
  - Popular location: New York, Palo Alto, Champaign IL.
- Support for endless scrolling

<img src='https://imgur.com/download/nm59Y35' title='Network' width='' alt='Video Walkthrough' />
### Business Detail page
- Show the restaurant image in the banner.
- Show main business information: restaurant name, rating, number of reviews, full list of categories, price, and distance.
- User can visit the official Yelp page for the restaurant.
- Show additional information: Address, business hours, phone number.
- Show up to 3 photo highlights of the restaurant.
<img src='https://imgur.com/download/39QvqDS' title='Network' width='' alt='Video Walkthrough' />
GIF created with [LiceCap](http://www.cockos.com/licecap/).

## Notes

Describe any challenges encountered while building the app.

## License

    Copyright [2020] [Qiwen Wang]

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

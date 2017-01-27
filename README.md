# PublicDomainBooks
An android app that allows users to search, download, and read public domain books.

-This app requires a pdf reader in order for the user to read the saved books.

-When the app is initially started, the main page will be empty as no books have been saved. 

-To search for books click on the plus symbol, enter your seach paramters and click "Search"

-Search information and book data comes from Google Books' API.

-Clicking on a book in the search screen launches a detailed view of the book. Clicking on the Save button immediately downloads the
thumbnail (png file) and JSON information (txt file) about the book. The user may have to fill in a captcha to actually download the 
pdf of the book.

-To delete books from the main screen, the user has to click on the delete icon in the menu. This turns delete mode on, indicated by the
background color change to red. Clicking on a book in this mode, deletes it. To turn delete mode off, the user has to click on the delete 
icon again.

-When a user clicks on a book in the main screen a detailed view of the book is shown. If the button says "File Not Downloaded", the pdf
file of the book is missing and needs to be re downloaded. Clicking the read button opens the file in a pdf reader if one is installed.

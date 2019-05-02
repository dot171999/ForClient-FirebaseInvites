# Firebase_Test_Repo

# Firebase screens configuration:
   All screens configurations should be placed in the "Screens" collection.
   
   The maximum hierarchy of screens should contain no more than two screens with buttons. 
   For example, Tutorial Screen (the screen with button) -> press "Basic Electronic" (the screen with buttons) -> "Breadboard" ( the screen with images)
   
   Now implemented 2 Activities for reading remote configurations. It contains in the documents:
    - UserGuide
    - Tutorials

  Every document should be content in the next fields:
	- type (buttons/picture) 
    - version

  If the type field equals "buttons", then this document should contain the next fields:
    - names (this field contains the buttons list for this screen)
   	- the name of the button from the list names where contain next fields, when this
	button will be pressed the next screen will be download from these parameters.
		- collection : "Collection name" 
		- document: "document name"
		- filed: "field name"
		- version
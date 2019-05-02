# Firebase_Test_Repo

# Firebase screens configuration:
   All screens configurations should be placed in the "Screens" collection.
   Now implemented 2 Activities for reading remote configurations. It contains in the documents:
    - UserGuide
    - Tutorials

  Every document should be content in the next fields:

   | FIELD | Description |
   |-------|-------------|
   |type| buttons/picture|
   |version| screen version number|
   |orientation| screen orientation landscape/portrait|

   if the type field equals "buttons":

   | FIELD | Description |
   |-------|-------------|
   |names| this field contains the buttons list for this screen|
   |next fields equal names from names list| |
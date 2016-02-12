The index has 2 string fields and one text field.
String Field “ItemID”  Field.Store.YES
String Field “Name”    Field.Store.NO
Text Field “Content” Field.Store.NO

The Name, Categories of item and Description are all concatenated into single string separated with white space, can are stored inside Content field. This way, we can search a keyword on name,categories and Description.
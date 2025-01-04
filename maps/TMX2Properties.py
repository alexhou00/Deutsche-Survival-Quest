import xml.etree.ElementTree as ET

# By using the tiled map editor TILED, we get a .tmx file
# the .tmx file is actually a type of .xml file
# this function reads the .xml file and convert it into the .properties file for the project
def xml_to_properties(xml_file, properties_file):
    tree = ET.parse(xml_file)
    root = tree.getroot()

    # Extract the data from the <data> tag in the <layer>
    layer = root.find("layer")
    data = layer.find("data").text.strip()

    # Get map dimensions
    width = int(root.get("width"))
    height = int(root.get("height"))

    # Parse CSV data
    rows = data.split("\n")
    properties_lines = []

    # Convert to .properties format
    for y, row in enumerate(reversed(rows)):
        tiles = row.split(",")
        for x, tile in enumerate(tiles):
            if tile != '':
                tile_value = int(tile)
                if tile_value != 0:  # Only include non-zero tiles
                    properties_lines.append(f"{x},{y}={tile_value-1}")  # minus one since our .properties file is 0-based

    # Write to .properties file
    with open(properties_file, "w") as f:
        f.write("\n".join(properties_lines))

dir_path = ""
xml_file = dir_path + input("Enter the filename of the tmx file (include the .tmx extension):") #"test.tmx"
properties_file = input("Enter the desired file name of the properties file (no need to include the ext.): ")
properties_file = dir_path + properties_file + ".properties"# "map.properties"
xml_to_properties(xml_file, properties_file)
print(f"Properties file generated: {properties_file}")

import xml.etree.ElementTree as ET

#Generate Messages
class CreateMessage:

    #Convert JSON to a message object
    def __init__(self, json):

        self.devices = []

        print('Creating the message')

        try:
            self.name = json['name']
        except:
            self.name = 'Generated name!'

        try:
            display = json['display']
            self.devices = display.split(',')
        except:
            display = ['LED_DISPLAY_1']
            self.devices.append(display)

        try:
            self.content = json['content']
            self.content.replace('/n', '').replace('/r', '')
        except:
            self.content = 'Generated content'

        try:
            self.size = json['size']
        except:
            self.size = '16'

        try:
            self.color = json['color']
        except:
            self.color = 'red'

        try:
            self.dwell = json['dwell']
        except:
            self.dwell = '10'

        try:
            self.presentation = json['presentation']
        except:
            self.presentation = 'appear'

        self.filename = GetId("msg_id")

        # self.payload = 'localmsgs compose -c "'+self.color +'" -b -d "' +self.dwell +'" -f "'+ self.filename + '" -p "' +self.presentation +'" -s "' +self.size +'" -t "' +self.content +'"'


class CreateDevice:

    #Convert JSON to a message object
    def __init__(self, json):

        try:
            self.name = json['name']
        except:
            self.name = 'Generated name!'

        try:
            self.bonjour = json['bonjour']
        except:
            self.bonjour = 'LED_DISPLAY_1'

        try:
            self.content = json['location']
        except:
            self.content = 'Generated content'

        self.id = GetId('dsp_id')

def GetId(file_type):

    XmlFile = ET.parse('Filenames.xml')
    Root = XmlFile.getroot()

    for element in Root:
        file_id = int(element.attrib[file_type])
        file_id = str(file_id + 1)
        element.attrib[file_type] = file_id
    XmlFile.write('Filenames.xml')

    return file_id

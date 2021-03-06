import urllib2
import base64
import xml.etree.ElementTree as ET

from Message_Class import CreateMessage, CreateDevice


# Create a message object from web input
def sendmessage(web_input):
    # Request the message object based on the web input
    message = CreateMessage(web_input)

    # try to broadcast the message
    try:
        # Sending the message with bonjour
        Update_Display_JSON(message)

        # Add the new message to the DB.
        AddEntry('Messages.xml', message)

        # Return success if message was sent
        return True

    except:
        # Return false if message was not sent
        return False


# Remove a message
def RemoveMessage(web_input):
    # Get the ID of the to be delete message
    message_id = str(web_input['message_id'])

    # Remove the message from the DB.
    return RemoveEntry('Messages.xml', str(message_id))


# Add a new device
def AddDevice(web_input):
    device = CreateDevice(web_input)

    AddEntry("Devices.xml", device)


# Remove a display
def RemoveDevice(web_input):
    print('PREPARING TO REMOVE A MESSAGE')

    device_id = web_input['device_id']

    return RemoveEntry('Devices.xml', str(device_id))


# Use the reversed engineered CGI module
def Update_Display_JSON(message):
    # Login params
    username = 'ontrack-web'
    password = '1n0v@'

    for display in message.devices:

        print(display)

        link = 'http://' + display + '.local/local_messaging/addMessage.cgi?messageFileName=' + message.filename + '&messageText=' + message.content.replace(
            ' ',
            '+') + '+%0D%0A&color=' + message.color + '&charSize=' + message.size + '&presentationMode=' + message.presentation + '&dwellTime=' + message.dwell + '&addPlaylist=Add+To+Playlist'

        # DEBUG
        print(link)

        request = urllib2.Request(link)
        base64string = base64.encodestring('%s:%s' % (username, password)).replace('\n', '')
        request.add_header("Authorization", "Basic %s" % base64string)
        try:
            # noinspection PyUnusedLocal
            response = urllib2.urlopen(request)
        except:
            return False


# Remove messages from the displays
def Remove_From_Display_JSON(displays, filename, delete_all):
    # Login params
    username = 'ontrack-web'
    password = '1n0v@'

    for display in displays:

        if delete_all:
            link = 'http://' + display + '.local/local_messaging/manage_messages.cgi?playlistMessageList=' + filename + '.llm&cancelAllPlaylist=Cancel+All'
        else:
            link = 'http://' + display + '.local/local_messaging/manage_messages.cgi?playlistMessageList=' + filename + '.llm&cancelPlaylist=Cancel'

        # DEBUG
        print(link)

        request = urllib2.Request(link)
        base64string = base64.encodestring('%s:%s' % (username, password)).replace('\n', '')
        request.add_header("Authorization", "Basic %s" % base64string)
        try:
            response = urllib2.urlopen(request)

            return True
        except:
            return False


# Parse xml files
def GetMessages(xml):

    XmlFile = ET.parse(xml)
    Root = XmlFile.getroot()

    # If the XML-File is empty a temporary element is added!
    if len(Root) == 0:

        if xml == 'Messages.xml':
            new_message = ET.Element('message', id="0", name="No messages!", content="There are no messages!",
                                     color="red", size="5", presentation="appear", dwell="10", displays="none")
        else:
            new_message = ET.Element('device', id="0", name="No display present!", location="Not defined", bonjour="0")
        Root.append(new_message)

    return Root


# Add new files to the DB
def AddEntry(xml, data):
    if xml == 'Messages.xml':
        Full_root = ET.parse(xml)

        root = Full_root.getroot()

        displays = ','.join(data.devices)

        new_message = ET.Element('message', id="" + data.filename + "", name="" + data.name + "",
                                 content="" + data.content + "", color="" + data.color + "", size="" + data.size + "",
                                 presentation="" + data.presentation + "", dwell="" + data.dwell + "",
                                 displays="" + displays + "")

        root.append(new_message)

        Full_root.write(xml)

    elif xml == 'Devices.xml':

        Full_root = ET.parse(xml)

        root = Full_root.getroot()

        new_device = ET.Element('device', id="" + data.id + "", name="" + data.name + "",
                                location="" + data.content + "", bonjour="" + data.bonjour + "")

        root.append(new_device)

        Full_root.write(xml)


# Remove messages from the DB
def RemoveEntry(xml, data_id):
    # parse the DB
    database = ET.parse(xml)

    entries = database.getroot()
    alldeleted = False


    for entry in entries.findall("*"):

        if xml == 'Messages.xml':

            if entry.attrib['id'] == data_id or data_id == 'all':

                displays = []

                try:
                    # Try to split displays for multiple displays
                    displays = entry.attrib['displays'].split(',')
                except:
                    # If it fails return origninal

                    displays.append(entry.attrib['displays'])

                if data_id == 'all':
                    Delete_All = True
                    if not alldeleted:
                       Remove_From_Display_JSON(displays, entry.attrib['id'], Delete_All)
                       alldeleted = True
                else:
                    Delete_All = False
                    Remove_From_Display_JSON(displays, entry.attrib['id'], Delete_All)

                entries.remove(entry)

        elif xml == 'Devices.xml':

            if entry.attrib['id'] == data_id:
                entries.remove(entry)

    database.write(xml)

    return True

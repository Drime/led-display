import web

from Broadcast_Interface import sendmessage, RemoveMessage, GetMessages, RemoveDevice, AddDevice


render = web.template.render('templates/')

# urls = ('/(.*)', 'index', '/add', 'add')

urls = ('/', 'index',
        '/add_message', 'add_message', '/add_device', 'add_device', '/remove_message', 'remove_message',
        '/remove_device', 'remove_device')



#Generates the main page
# noinspection PyClassHasNoInit
class index:
    @staticmethod
    def GET():
        i = web.input(i=0, broadcast_stat=None, remove_stat=None, display_stat=None)

        messages = GetMessages('Messages.xml')
        devices = GetMessages('Devices.xml')

        print("READ THE DATABASES")

        return render.index(i.i, i.broadcast_stat, i.remove_stat, i.display_stat, messages, devices)

#Add a new message to the device and DB /add_message?....
# noinspection PyClassHasNoInit
class add_message:
    @staticmethod
    def POST():


        if sendmessage(web.input()):


            raise web.seeother('/?broadcast_stat=true#messages')

        else:

            raise web.seeother('/?broadcast_stat=false#messages')


#Removes messages from the device(s) and from the DB /remove_device?...
# noinspection PyClassHasNoInit
class remove_message:
    @staticmethod
    def POST():

        print('Captured post!')

        if RemoveMessage(web.input()):


            raise web.seeother('/?remove_stat=true#messages')

        else:

            raise web.seeother('/?remove_stat=false#messages')


#Removes messages from the device(s) and from the DB /remove_device?...
# noinspection PyClassHasNoInit
class remove_device:
    @staticmethod
    def POST():

        print('Captured post!')

        if RemoveDevice(web.input()):


            raise web.seeother('/?device_stat=true#displays')

        else:

            raise web.seeother('/?device_stat=false#displays')


#Add a new device to the DB /add_device?...
# noinspection PyClassHasNoInit
class add_device:
    @staticmethod
    def POST():

        if AddDevice(web.input()):


            raise web.seeother('/?device_stat=true#displays')

        else:

            raise web.seeother('/?device_stat=false#displays')

#Start the webserver on script launch!
if __name__ == "__main__":

    app = web.application(urls, globals())
    app.run()


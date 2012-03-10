#! /usr/bin/env python
import urllib2, re

from HTMLParser import HTMLParser

class MyHTMLParser(HTMLParser):

    def __init__(self, url, name):
        """
        {fh} must be an input stream returned by open() or urllib2.urlopen()
        """
        HTMLParser.__init__(self)
        self.name = name
        self.names = []
        self.servers = []
        self.div = False
        self.p_tag_count = 0
        self.server_p_tag = False
        self.a_tag = False
        self.data_count = 0
        
        # start
        opener = urllib2.build_opener(urllib2.HTTPHandler(debuglevel=0))
        opener.addheaders = [('User-agent', 'Mozilla/5.0')]
        response = opener.open(url)
        self.feed(response.read())

    def handle_starttag(self, tag, attrs):
        if (tag == 'div'):
            for name, value in attrs:
                if (name == 'class' and value == 'block'):
                    #print "in div now!"
                    self.div = True
        
        if (tag == 'p' and self.div == True):
            self.p_tag_count += 1
            #print "p tag counter: %s" % self.p_tag_count
            
            # 7th p tag contains servers
            if (self.p_tag_count == 7):
                #print "7th p tag opened!"
                self.server_p_tag = True
            
        if (tag == 'a' and self.server_p_tag == True):
            self.a_tag = True

        
    def handle_endtag(self, tag):
        if (tag == 'a' and self.server_p_tag == True):
            self.a_tag = False
            
        # 7th p tag contains servers
        if (tag == 'p' and self.p_tag_count == 7):
            #print "7th p tag closed!"
            self.server_p_tag = False
    
    def handle_data(self, data):        
        if (self.server_p_tag == True):
            self.data_count += 1
            
        if (self.data_count == 2):
            p = re.compile('.*\s+(\S+)\s+.*')
            m = p.match(data)
            if m:
                #print 'Match found: ', m.group(1)
                self.servers.append(m.group(1))

            #else:
                #print 'No match'
                
        if (self.a_tag):
            self.names.append(data)
            self.data_count = 0
            
    def print_xml(self):
        print "<string-array name=\"pref_ntp_server_entries_%s\">" % self.name
        for name in self.names:
            print "<item>%s</item>" % name
        print "</string-array>"
        print "<string-array name=\"pref_ntp_server_entries_%s_values\" translate=\"false\">" % self.name
        for server in self.servers:
            print "<item>%s</item>" % server
        print "</string-array>"
        
        
europe = MyHTMLParser("http://www.pool.ntp.org/zone/europe", "europe")
europe.print_xml()

asia = MyHTMLParser("http://www.pool.ntp.org/zone/asia", "asia")
asia.print_xml()

oceania = MyHTMLParser("http://www.pool.ntp.org/zone/oceania", "oceania")
oceania.print_xml()

north_america = MyHTMLParser("http://www.pool.ntp.org/zone/north-america", "north_america")
north_america.print_xml()

south_america = MyHTMLParser("http://www.pool.ntp.org/zone/south-america", "south_america")
south_america.print_xml()

africa = MyHTMLParser("http://www.pool.ntp.org/zone/africa", "africa")
africa.print_xml()



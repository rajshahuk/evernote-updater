package com.twelvenines.evernote.noteupdater;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.clients.UserStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.TException;

public class App {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

	public static Properties readAuth() throws Exception {
        Properties props = new Properties();
		props.load(App.class.getClassLoader().getResourceAsStream(".auth"));
		return props;
	}
	
	public static void main( String[] args ) throws Exception {
		Properties props = readAuth();
		String AUTH_TOKEN = props.getProperty("AUTH_TOKEN");

        EvernoteAuth evernoteAuth = new EvernoteAuth(EvernoteService.PRODUCTION, AUTH_TOKEN);
        ClientFactory factory = new ClientFactory(evernoteAuth);
        UserStoreClient userStore = factory.createUserStoreClient();
        NoteStoreClient noteStore = factory.createNoteStoreClient();
        
        System.out.println("Logged in");
        
        Notebook defaultNoteBook = noteStore.getDefaultNotebook();
        System.out.println("Default Notebook: " + defaultNoteBook.getName());
        
        listNotebooks(noteStore);
        
        updateCreationDate(noteStore, defaultNoteBook);
    }

	private static void listNotebooks(NoteStoreClient noteStore)
			throws EDAMUserException, EDAMSystemException, TException {
		List<Notebook> noteBooks = noteStore.listNotebooks();
        
        for (Notebook noteBook : noteBooks) {
        	System.out.println(noteBook.getName() + ":" + noteBook.getGuid());
        }
	}

	private static void updateCreationDate(NoteStoreClient noteStore, Notebook defaultNoteBook)
			throws EDAMUserException, EDAMSystemException, TException,
			EDAMNotFoundException {
		
		
        NoteFilter filter = new NoteFilter();
        filter.setOrder(NoteSortOrder.CREATED.getValue());
        filter.setAscending(false);
        filter.setNotebookGuid(defaultNoteBook.getGuid());
        NoteList noteList = noteStore.findNotes(filter, 0, 50);
        List<Note> notes = noteList.getNotes();
        for(Note note : notes) {
        	String noteTitle = note.getTitle();
        	try {
	        	Date creationDate = sdf.parse(noteTitle.substring(0, 8));
	        	System.out.println("Note: " + note.getTitle() + " Date: " + creationDate);
	        	if(note.getCreated() == creationDate.getTime()) {
	        		System.out.println("skipping...");
	        	}
	        	else {
		        	note.setCreated(creationDate.getTime());
		        	note.setUpdated(creationDate.getTime());
		        	noteStore.updateNote(note);
		        	System.out.println("updated note!");
	        	}
        	}
        	catch(ParseException pe) {
        		System.out.println("Unable to set date for: " + noteTitle);
        	}
        	catch(StringIndexOutOfBoundsException stringIndexOutOfBoundsException) {
        		System.out.println("Unable to set date for: " + noteTitle);
        	}
        }
	}
}

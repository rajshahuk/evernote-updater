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

    public static void main(String[] args) throws Exception {
        Properties props = readAuth();
        String AUTH_TOKEN = props.getProperty("AUTH_TOKEN");

        EvernoteAuth evernoteAuth = new EvernoteAuth(EvernoteService.PRODUCTION, AUTH_TOKEN);
        ClientFactory factory = new ClientFactory(evernoteAuth);
        UserStoreClient userStore = factory.createUserStoreClient();
        NoteStoreClient noteStore = factory.createNoteStoreClient();

        System.out.println("Logged in");

        Notebook defaultNoteBook = noteStore.getDefaultNotebook();
        System.out.println("Default Notebook: " + defaultNoteBook.getName());
        List<Notebook> noteBooks = noteStore.listNotebooks();

        noteBooks.stream().iterator().forEachRemaining(notebook -> {
            try {
                updateCreationDate(noteStore, notebook, false);
                System.out.println("[%s] [%s]".formatted(notebook.getGuid(), notebook.getName()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void listNotebooks(NoteStoreClient noteStore)
            throws EDAMUserException, EDAMSystemException, TException {
        List<Notebook> noteBooks = noteStore.listNotebooks();

        for (Notebook noteBook : noteBooks) {
            System.out.println(noteBook.getName() + ":" + noteBook.getGuid());
        }
    }

    private static void updateCreationDate(NoteStoreClient noteStore, Notebook notebook, boolean debug)
            throws EDAMUserException, EDAMSystemException, TException,
            EDAMNotFoundException {


        System.out.println("Procesing notebook: [%s]".formatted(notebook.getName()));
        NoteFilter filter = new NoteFilter();
        filter.setOrder(NoteSortOrder.CREATED.getValue());
        filter.setAscending(false);
        filter.setNotebookGuid(notebook.getGuid());
        int skipped = 0, updated = 0, notUpdated = 0, processedNotes = 0;
        int offset = 0;
        int maxNotes = 50;
        boolean stop = false;
        while (!stop) {
            NoteList noteList = noteStore.findNotes(filter, offset, maxNotes);
            if (!stop) {
                int batchSize = noteList.getNotesSize();
                System.out.println("Processing batch size [%s]".formatted(batchSize));
                stop = !(batchSize == maxNotes);
                List<Note> notes = noteList.getNotes();
                for (Note note : notes) {
                    String noteTitle = note.getTitle();
                    try {
                        processedNotes++;
                        Date creationDate = sdf.parse(noteTitle.substring(0, 8));
                        if (debug) System.out.println("Note: " + note.getTitle() + " Date: " + creationDate);
                        if (note.getCreated() == creationDate.getTime() && note.getUpdated() == creationDate.getTime()) {
                            skipped++;
                        } else {
                            note.setCreated(creationDate.getTime());
                            note.setUpdated(creationDate.getTime());
                            noteStore.updateNote(note);
                            updated++;
                        }
                    } catch (ParseException pe) {
                        if(debug) System.out.println("Not updating: [%s]".formatted(pe.getMessage()));
                        notUpdated++;
                    } catch (StringIndexOutOfBoundsException stringIndexOutOfBoundsException) {
                        if(debug) System.out.println("Not updating: [%s]".formatted(stringIndexOutOfBoundsException.getMessage()));
                        notUpdated++;
                    }
                }
                offset += maxNotes;
            }
        }
        System.out.println(
                """
                        Notebook	: %s
                        Processed	: %s
                        Updated		: %s
                        Skipped		: %s
                        Failed    	: %s
                        				""".formatted(notebook.getName(), processedNotes, updated, skipped, notUpdated));


    }
}

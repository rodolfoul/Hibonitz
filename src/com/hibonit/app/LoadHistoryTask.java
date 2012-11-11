package com.hibonit.app;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.Note;

public class LoadHistoryTask extends AsyncTask<Void, Void, ArrayAdapter<SumarioCorrida>> {

	private History mActivity = null;

	// ---------------------------------------------------------------------------------------------
	// Constructors
	// ---------------------------------------------------------------------------------------------

	public LoadHistoryTask(History activity) {
		this.mActivity = activity;
	}

	protected void onPostExecute(SumarioCorridaAdapter result) {
		super.onPostExecute(result);
		this.mActivity.setAdapter(result);
	}

	@Override
	protected ArrayAdapter<SumarioCorrida> doInBackground(Void... arg0) {
		// Local variables
		ArrayAdapter<SumarioCorrida> adapter = null;
		List<SumarioCorrida> corridas = new ArrayList<SumarioCorrida>();
		retrieveHistory(corridas);
		Log.v("Hibonit", "Corridas: " + corridas.size());
		adapter = new SumarioCorridaAdapter(this.mActivity, R.layout.linha_history, corridas);
		Log.v("Hibonit", "Adapter: " + adapter.getCount());
		onPostExecute(adapter);
		return adapter;
	}

	private void retrieveHistory(List<SumarioCorrida> corridas) {
		NoteFilter f = new NoteFilter();

		f.setWords("contentClass:hibonit.corrida");

		try {
			NotesMetadataList notes = Evernote.getNoteStore().findNotesMetadata(Evernote.getAuthToken(), f, 0, 10, new NotesMetadataResultSpec());

			for (NoteMetadata note : notes.getNotes()) {
				Note fullNote;
				try {
					 fullNote = Evernote.getNoteStore().getNote(Evernote.getAuthToken(), note.getGuid(), true, true, false, false);
				} catch (Exception e) {
					continue;
				}
				corridas.add(new SumarioCorrida(fullNote, false));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

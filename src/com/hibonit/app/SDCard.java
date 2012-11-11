package com.hibonit.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

import android.os.Environment;

public class SDCard {
	boolean available = false;
	boolean writeable = false;
	File file;

	public SDCard(String packageName, String fileName) { // fileName must
															// include
															// extension!!
		checkSDCard(); // sets the values of the variables �available� and
						// �writable�
		if (available) { // if SD Card can be read
			File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() // gets
																								// root
																								// of
																								// the
																								// SD
					+ "/Android/data/" + packageName + "/files"); // inside
																	// "files"
																	// folder of
																	// the app
			path.mkdirs(); // make sure that folder exists

			file = new File(path, fileName);
		}
	}

	public void write(String text) { // erases previous text and writes new text
		if (writeable) { // if it's possible to write on the SDCard
			try {
				if (!file.exists()) {
					file.createNewFile(); // create file if it doesn't exist
				}
				FileWriter fileWriter = new FileWriter(file);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(text); // writes the String text in the
											// file
				bufferedWriter.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void append(String text) { // adds text to the end of the file, does
										// not erase previous text
		if (writeable) { // if it's possible to write on the SDCard
			try {
				if (!file.exists()) {
					file.createNewFile(); // create file if it doesn't exist
				}
				FileWriter fileWriter = new FileWriter(file, true); // because
																	// of
																	// parameter
																	// �true�
				// will add to the end of the file instead of overwriting
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(text); // appends the String text to the
											// end of the file
				bufferedWriter.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void erase() {
		write(""); // everytime �write� is called, it erases previous text
		// so this erases without writing anything else
	}

	public void delete() {
		if (writeable) { // if it's possible to write on the SDCard
			if (file.exists()) // if the file exists
				file.delete(); // delete the file
		}
	}

	public String read() {
		String row = "";
		String content = "";

		if (available) { // if SDCard is available
			if (file.exists()) { // if file exists
				try {
					FileInputStream fileInputStream = new FileInputStream(file);
					InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
					while ((row = bufferedReader.readLine()) != null) { // until
																		// the
																		// last
																		// line
																		// of
																		// the
																		// file
						// (when row will be null)
						content = content + row + "\n"; // content string will
														// contain everything
														// that
						// has been read so far
					}
					bufferedReader.close();
					inputStreamReader.close();
					fileInputStream.close(); // close everything we used to read
												// the file
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return content; // if SDCard is not available or if
		// file doesn't exist, an empty string will be returned
		// because content was defined as an empty String in the beginning.
	}

	public void checkSDCard() { // sets the values of the variables �available�
								// and �writable�

		String state = Environment.getExternalStorageState(); // gets the state
																// of the
																// external
																// storage

		if (Environment.MEDIA_MOUNTED.equals(state)) { // compares to see if
														// it�s the state of
														// mounted media
			// We can read and write the media
			available = true;
			writeable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) { // or if
																		// it's
																		// the
																		// state
																		// of
																		// read
																		// only
			// We can only read the media
			available = true;
			writeable = false;
		} else { // or if it�s any other state
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			available = false;
			writeable = false;
		}
	}

	public boolean isWriteable() {
		return writeable;
	}

	public boolean isAvailable() {
		return available;
	}

	public boolean fileExists() {
		return file.exists();
	}

}
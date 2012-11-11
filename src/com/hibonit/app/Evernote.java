package com.hibonit.app;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.evernote.client.conn.ApplicationInfo;
import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.notestore.NoteStore.Client;
import com.evernote.edam.type.Note;

/*
 * Controla a sincronização com o Evernote
 * Primeiro, dê o init() e 
 */
public class Evernote {
	// Constantes para identificação
	private static final String CONSUMER_KEY = "julia-at";
	private static final String CONSUMER_SECRET = "4a92f651fd1ac867";
	private static final String EVERNOTE_HOST = "sandbox.evernote.com";
	private static final String APP_NAME = "Hibonit";
	private static final String APP_VERSION = "1.0a";
	private static final String APP_DATA_PATH = "/Android/data/com.evernote.android.sample/temp/";
	
	private static Context contexto;
	private static SharedPreferences dados;
	private static EvernoteSession sessao;
	
	// Retorna o status da conexão
	public static boolean estaConectado() {
		return sessao.isLoggedIn();
	}
	
	// Carrega os dados da sessão já salvos no celular
	public static void init(Context contextoApplicacao) {
		ApplicationInfo info = new ApplicationInfo(CONSUMER_KEY, CONSUMER_SECRET, EVERNOTE_HOST, APP_NAME, APP_VERSION);
		File tempDir = new File(Environment.getExternalStorageDirectory(), APP_DATA_PATH);
		contexto = contextoApplicacao;
		dados = contexto.getSharedPreferences("evernoteSession", Context.MODE_PRIVATE);
		sessao = new EvernoteSession(info, dados, tempDir);
		sessao.completeAuthentication(dados);
	}
	
	// Faz o login
	public static void login() {
		if (!sessao.isLoggedIn()){
			sessao.authenticate(contexto);
		}
	}
	
	// Completa o login
	public static void terminarLogin() {
		sessao.completeAuthentication(dados);
	}
	
	// Faz o logout
	public static void logout() {
		if (sessao.isLoggedIn())
			sessao.logOut(dados);
	}
	
	// Retorna a sessão (ou null em caso de erro)
	public static Client getNoteStore() {
		try {
			Log.v("hibonit", "token:" + sessao.getAuthToken());
			Log.v("hibonit", "logged: " + String.valueOf(sessao.isLoggedIn()));
			Log.v("hibonit", "token:" + sessao.toString());
			return sessao.createNoteStore();
		} catch (Exception e) {
			Log.v("hibonit", "error getNoteStore: " + e.getMessage() );
			e.printStackTrace();
			return null;
		}
	}
	
	// Retorna a chave de autenticação
	public static String getAuthToken() {
		return sessao.getAuthToken();
	}
	
	// Envia ou atualiza uma nota
	// Retorna true em caso de sucesso, false em caso de erro
	public static boolean enviar(Note nota) {
		try {
			if (!nota.isSetGuid()) {
				// Nova nota
				getNoteStore().createNote(getAuthToken(), nota);
			} else {
				// Nota antiga
				getNoteStore().updateNote(getAuthToken(), nota);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}

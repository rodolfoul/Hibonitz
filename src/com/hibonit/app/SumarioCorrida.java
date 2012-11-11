package com.hibonit.app;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.evernote.client.conn.mobile.FileData;
import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteAttributes;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.ResourceAttributes;
import com.evernote.edam.util.EDAMUtil;

/*
 * Classe que reúne todos os dados de uma corrida
 * Permite importar/exportar de uma nota do Evernote
 */
public class SumarioCorrida {
	// Mime-type do .kml
	public static final String KML_MIME_TYPE = "application/vnd.google-earth.kml+xml";
	
	// Dados armazenados
	public int calorias = 0; // kcal
	public int suor = 0; // ml
	public double distancia = 0; // m
	public int tempo = 0; // s
	public double velocidade = 0; // média em km/h
	public String graficoUrl = "";
	public Date data = null;
	public int clima = 0; // TODO: definir um formato
	public String rota = ""; // Conteúdo do arquivo do .kml
	public String comentarios = ""; // Comentários da pessoa
	private Note nota = null; // Faz referência à nota salva
	private SDCard arquivo = null; // Armazena a referência para o arquivo temporário
	
	// Importa os dados de uma nota
	// Gera uma excessão se a nota for inválida (mal formatada)
	// Se getKml for true, irá carregar o arquivo e salvá-lo no SD
	public SumarioCorrida(Note nota, boolean carregarAnexos) throws Exception {
		this.nota = nota;
		
		// Pega os valores inteiros
		Document doc = ler();
		NodeList spans = doc.getElementsByTagName("span");
		for (int i=0; i<spans.getLength(); i++) {
			Element el = (Element)spans.item(i);
			String str = el.getTextContent();
			String name = el.getAttribute("style");
			if (name.startsWith("hack-calorias"))
				calorias = Integer.parseInt(str);
			else if (name.startsWith("hack-suor"))
				suor = Integer.parseInt(str);
			else if (name.startsWith("hack-distancia"))
				distancia = Double.parseDouble(str);
			else if (name.startsWith("hack-tempoH"))
				tempo += Integer.parseInt(str)*3600;
			else if (name.startsWith("hack-tempoM"))
				tempo += Integer.parseInt(str)*60;
			else if (name.startsWith("hack-tempoS"))
				tempo += Integer.parseInt(str);
			else if (name.startsWith("hack-velocidade"))
				velocidade = Double.parseDouble(str);
			else if (name.startsWith("hack-clima"))
				clima = Integer.parseInt(str);
			else if (name.startsWith("hack-comentarios"))
				comentarios = str;
			else if (name.startsWith("hack-data")) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
				data = sdf.parse(str);
			}
		}
		
		// Pega o arquivo kml
		if (carregarAnexos) {
			Resource kml = getKml();
			if (kml == null)
				throw new Exception("Arquivo .kml não encontrado");
			byte[] body;
			if (kml.isSetData())
				body = kml.getData().getBody();
			else
				body = Evernote.getNoteStore().getResourceData(Evernote.getAuthToken(), kml.getGuid());
			rota = new String(body);
			
			// Salva no SD
			arquivo = criarArquivo();
			arquivo.write(rota);
		}
	}
	
	// Cria uma corrida vazia
	public SumarioCorrida(String titulo) {
		nota = new Note();
		nota.setTitle(titulo);
		nota.setContent("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\"><en-note>\n\t<div style=\"padding-bottom:0;background-color:#fff;margin:0 auto;padding-left:0;padding-right:0;max-width:600px;PADDING-TOP: 25px; box-shadow: 0 0px 5px rgba(0,0,0,0.2);line-height:1.3;font-family:'Helvetica Neue', Helvetica,Arial,'Liberation Sans',FreeSans,sans-serif;color:#585957;font-size:14px\">\n\t\t<div style=\"PADDING-BOTTOM: 15px; MARGIN: 0px 25px\">\n\t\t\t<h1 style=\"MARGIN: 15px 0px 0px; COLOR: #5fb336; FONT-SIZE: 20px; FONT-WEIGHT: normal\">Sumário da corrida</h1>\n\t\t</div>\n\t\t\n\t\t<div style=\"MARGIN: 0px 25px\">\n\t\t\t<h2 style=\"LINE-HEIGHT: 1.25em; MARGIN-BOTTOM: 12px; FONT-SIZE: 16px; PADDING-TOP: 8px\">Trajeto\tpercorrido</h2>\n\t\t</div>\n\t\t\n\t\t<div style=\"MARGIN: 20px 25px\">\n\t\t\t<span style=\"hack-comentarios\"></span>\n\t\t</div>\n\t\t\n\t\t<div style=\"BORDER-BOTTOM: #5fb336 1px solid; BORDER-LEFT: #5fb336 1px solid; PADDING-BOTTOM: 8px; BACKGROUND-COLOR: #ccff99; MARGIN: 0px 25px; PADDING-LEFT: 15px; PADDING-RIGHT: 15px; COLOR: #4d4b47; CLEAR: both; FONT-SIZE: 14px; BORDER-TOP: #5fb336 1px solid; BORDER-RIGHT: #5fb336 1px solid; PADDING-TOP: 0px\">\n\t\t\t<en-media style=\"hack-linkKml\" type=\"application/vnd.google-earth.kml+xml\" />\n\t\t</div>\n\t\t\n\t\t<p>&nbsp;</p>\n\t\t\n\t\t<div style=\"MARGIN: 0px 25px\">\n\t\t\t<div>\n\t\t\t\t<div style=\"MIN-WIDTH: 200px; MARGIN: 0px 0px 8px; MIN-HEIGHT: 35px; WIDTH: 100%; PADDING-RIGHT: 0px; DISPLAY: block; FLOAT: left\">\n\t\t\t\t\t<div style=\"width: 50%;min-width: 200px;float: left;margin: 0 0px 8px 0px;padding-right: 0px;\">\n\t\t\t\t\t\t<div style=\"text-decoration: none;line-height: 1em;color: #6f6f6f;position: relative; display: block;\">\t\n\t\t\t\t\t\t\t<img alt=\"Clock Icon\" src=\"data:image/png;base64,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\" style=\"position:absolute;top:0;left:0;border-color:transparent\" />\n\t\t\t\t\t\t\t\t<div style=\"width: 50%;min-width: 200px;margin: 0 0px 8px 0px;padding-right: 10px;line-height: .8em;margin-left: 34px;\">\n\t\t\t\t\t\t\t\t\t<span style=\"display: block;font-size: 14px;margin-bottom: .3em;font-weight: bold;\">Duração</span>\n\t\t\t\t\t\t\t\t\t<span style=\"font-size: 12px;width: auto;\"><span style=\"hack-tempoH\"></span>:<span style=\"hack-tempoM\"></span>:<span style=\"hack-tempoS\"></span></span>\n\t\t\t\t\t\t\t  </div>\n\t\t\t\t\t\t</div>\n\t\t\t\t\t</div>\n\t\t\t\t\t\t<div style=\"width: 50%;min-width: 200px;float: left;margin: 0 0px 8px 0px;padding-right: 0px;\">\n\t\t\t\t\t\t<div style=\"text-decoration: none;line-height: 1em;color: #6f6f6f;position: relative; display: block;\">\t\n\t\t\t\t\t\t\t<img alt=\"Clock Icon\" src=\"data:image/png;base64,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\" style=\"position:absolute;top:0;left:0;border-color:transparent\" />\n\t\t\t\t\t\t\t\t<div style=\"width: 50%;min-width: 200px;margin: 0 0px 8px 0px;padding-right: 10px;line-height: .8em;margin-left: 34px;\">\n\t\t\t\t\t\t\t\t\t<span style=\"display: block;font-size: 14px;margin-bottom: .3em;font-weight: bold;\">Distância</span>\n\t\t\t\t\t\t\t\t\t<span style=\"font-size: 12px;width: auto;\"><span style=\"hack-distancia\"></span> m</span>\n\t\t\t\t\t\t\t  </div>\n\t\t\t\t\t\t</div>\n\t\t\t\t\t</div>\n\t\t\t\t</div>\n\t\t\t\t\n\t\t\t\t<div style=\"MIN-WIDTH: 200px; MARGIN: 0px 0px 8px; MIN-HEIGHT: 35px; WIDTH: 100%; PADDING-RIGHT: 0px; DISPLAY: block; FLOAT: left\">\n\t\t\t\t\t<div style=\"width: 50%;min-width: 200px;float: left;margin: 0 0px 8px 0px;padding-right: 0px;\">\n\t\t\t\t\t\t<div style=\"text-decoration: none;line-height: 1em;color: #6f6f6f;position: relative; display: block;\">\t\n\t\t\t\t\t\t\t<img alt=\"Clock Icon\" src=\"data:image/png;base64,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\" style=\"position:absolute;top:0;left:0;border-color:transparent\" />\n\t\t\t\t\t\t\t\t<div style=\"width: 50%;min-width: 200px;margin: 0 0px 8px 0px;padding-right: 10px;line-height: .8em;margin-left: 34px;\">\n\t\t\t\t\t\t\t\t\t<span style=\"display: block;font-size: 14px;margin-bottom: .3em;font-weight: bold;\">Velocidade</span>\n\t\t\t\t\t\t\t\t\t<span style=\"font-size: 12px;width: auto;\"><span style=\"hack-velocidade\"></span> km/h</span>\n\t\t\t\t\t\t\t  </div>\n\t\t\t\t\t\t</div>\n\t\t\t\t\t</div>\n\t\t\t\t\n\t\t\t\t\t<div style=\"width: 50%;min-width: 200px;float: left;margin: 0 0px 8px 0px;padding-right: 0px;\">\n\t\t\t\t\t\t<div style=\"text-decoration: none;line-height: 1em;color: #6f6f6f;position: relative; display: block;\">\t\n\t\t\t\t\t\t\t<img alt=\"Clock Icon\" src=\"data:image/png;base64,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\" style=\"position:absolute;top:0;left:0;border-color:transparent\" />\n\t\t\t\t\t\t\t\t<div style=\"width: 50%;min-width: 200px;margin: 0 0px 8px 0px;padding-right: 10px;line-height: .8em;margin-left: 34px;\">\n\t\t\t\t\t\t\t\t\t<span style=\"display: block;font-size: 14px;margin-bottom: .3em;font-weight: bold;\">Clima</span>\n\t\t\t\t\t\t\t\t\t<span style=\"font-size: 12px;width: auto;\"><span style=\"hack-clima\"></span></span>\n\t\t\t\t\t\t\t  </div>\n\t\t\t\t\t\t</div>\n\t\t\t\t\t</div>\n\t\t\t\t</div>\n\t\t\t</div>\n\t\t\t\n\t\t\t<div style=\"MIN-WIDTH: 200px; MARGIN: 0px 0px 8px; MIN-HEIGHT: 35px; WIDTH: 100%; PADDING-RIGHT: 0px; DISPLAY: block; FLOAT: left\">\n\t\t\t\t<div style=\"width: 50%;min-width: 200px;float: left;margin: 0 0px 8px 0px;padding-right: 0px;\">\n\t\t\t\t\t<div style=\"text-decoration: none;line-height: 1em;color: #6f6f6f;position: relative; display: block;\">\t\n\t\t\t\t\t\t<img alt=\"Clock Icon\" src=\"data:image/png;base64,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\" style=\"position:absolute;top:0;left:0;border-color:transparent\" />\n\t\t\t\t\t\t\t<div style=\"width: 50%;min-width: 200px;margin: 0 0px 8px 0px;padding-right: 10px;line-height: .8em;margin-left: 34px;\">\n\t\t\t\t\t\t\t\t<span style=\"display: block;font-size: 14px;margin-bottom: .3em;font-weight: bold;\">Transpiração</span>\n\t\t\t\t\t\t\t\t<span style=\"font-size: 12px;width: auto;\"><span style=\"hack-suor\"></span> ml</span>\n\t\t\t\t\t\t  </div>\n\t\t\t\t\t</div>\n\t\t\t\t</div>\n\t\t\t\t\n\t\t\t\t<div style=\"width: 50%;min-width: 200px;float: left;margin: 0 0px 8px 0px;padding-right: 0px;\">\n\t\t\t\t\t<div style=\"text-decoration: none;line-height: 1em;color: #6f6f6f;position: relative; display: block;\">\t\n\t\t\t\t\t\t<img alt=\"Clock Icon\" src=\"data:image/png;base64,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\" style=\"position:absolute;top:0;left:0;border-color:transparent\" />\n\t\t\t\t\t\t\t<div style=\"width: 50%;min-width: 200px;margin: 0 0px 8px 0px;padding-right: 10px;line-height: .8em;margin-left: 34px;\">\n\t\t\t\t\t\t\t\t<span style=\"display: block;font-size: 14px;margin-bottom: .3em;font-weight: bold;\">Calorias</span>\n\t\t\t\t\t\t\t\t<span style=\"font-size: 12px;width: auto;\"><span style=\"hack-calorias\">?</span> kcal</span>\n\t\t\t\t\t\t  </div>\n\t\t\t\t\t</div>\n\t\t\t\t</div>\n\t\t\t</div>\n\t\t\t\n\t\t\t<div style=\"MIN-WIDTH: 200px; MARGIN: 0px 0px 8px; MIN-HEIGHT: 35px; WIDTH: 100%; PADDING-RIGHT: 0px; DISPLAY: block; FLOAT: left\">\n\t\t\t\t<div style=\"width: 50%;min-width: 200px;float: left;margin: 0 0px 8px 0px;padding-right: 0px;\">\n\t\t\t\t\t<div style=\"text-decoration: none;line-height: 1em;color: #6f6f6f;position: relative; display: block;\">\t\n\t\t\t\t\t\t<img alt=\"Clock Icon\" src=\"data:image/png;base64,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\" style=\"position:absolute;top:0;left:0;border-color:transparent\" />\n\t\t\t\t\t\t\t<div style=\"width: 50%;min-width: 200px;margin: 0 0px 8px 0px;padding-right: 10px;line-height: .8em;margin-left: 34px;\">\n\t\t\t\t\t\t\t\t<span style=\"display: block;font-size: 14px;margin-bottom: .3em;font-weight: bold;\">Data e hora</span>\n\t\t\t\t\t\t\t\t<span style=\"hack-data;font-size: 12px;width: auto;\"></span>\n\t\t\t\t\t\t  </div>\n\t\t\t\t\t</div>\n\t\t\t\t</div>\n\t\t\t</div>\n\t\t</div>\n\t\t<div style=\"BORDER-BOTTOM: #5fb336 1px solid; BORDER-LEFT: #5fb336 1px solid; PADDING: 8px 15px; BACKGROUND-COLOR: #ccff99; MARGIN: 0px 25px; COLOR: #4d4b47; CLEAR: both; FONT-SIZE: 14px; BORDER-TOP: #5fb336 1px solid; BORDER-RIGHT: #5fb336 1px solid\">\n\t\t\t<img src=\"?\" style=\"hack-grafico; width:100%\" alt=\"Gráfico\" />\n\t\t</div>\n\t\t<p>&nbsp;</p>\n\t\t<span style=\"hack-imagens;BORDER-BOTTOM: #5fb336 1px solid; BORDER-LEFT: #5fb336 1px solid; PADDING: 8px 15px; BACKGROUND-COLOR: #ccff99; MARGIN: 0px 25px; COLOR: #4d4b47; CLEAR: both; FONT-SIZE: 14px; BORDER-TOP: #5fb336 1px solid; BORDER-RIGHT: #5fb336 1px solid\">\n\t\t\t\n\t\t</span>\n\t\t<p>&nbsp;</p>\n\t</div>\n\t\n</en-note>");
		NoteAttributes atributos = new NoteAttributes();
		atributos.setContentClass("hibonit.corrida");
		nota.setAttributes(atributos);
		data = new Date();
		arquivo = criarArquivo();
	}
	
	// Retorna o XML como HTML para ser exibido num WebView
	public String toHTML() {
		// Lê para DOM
		try {
			Document doc = ler();
			NodeList nos = doc.getElementsByTagName("en-media");
			Element antigo;
			int i;
			
			// Troca as tags en-media para img (quando possível)
			// Troca a tag en-media do .kml para link
			for (i=0; i<nos.getLength(); i++) {
				antigo = (Element)nos.item(i);
				if (antigo.getAttribute("type").startsWith("image/"))
					setTagName(antigo, "img");
				else if (antigo.getAttribute("type").equals(KML_MIME_TYPE) && arquivo != null) {
					Element a = doc.createElement("a");
					a.setAttribute("href", "geo:0,0?q="+URLEncoder.encode(arquivo.file.getAbsolutePath(), "UTF-8"));
					a.appendChild(doc.createTextNode("Abrir pelo Maps"));
					antigo.getParentNode().insertBefore(a, antigo);
				} else
					antigo.getParentNode().removeChild(antigo);
			}
			
			// Troca a tag base (en-note => body)
			setTagName((Element)doc.getElementsByTagName("en-note").item(0), "body");;
			
			return XML2String(doc);
		} catch (Exception e) {
			return "";
		}
	}
	
	// Retorna o endereço do arquivo no SD ("" caso não disponível)
	public String getKmlPath() {
		if (arquivo == null)
			return "";
		else
			return arquivo.file.getAbsolutePath();
	}
	
	// Adiciona uma imagem à nota
	// Retorna true em caso de sucesso
	public boolean adicionarImagem(Context contexto, Uri caminho) {
		try {
			String[] queryColumns = { MediaStore.Images.Media.DATA, 
                    MediaStore.Images.Media.MIME_TYPE, 
                    MediaStore.Images.Media.DISPLAY_NAME };
			Cursor cursor = contexto.getContentResolver().query(caminho, queryColumns, null, null, null);
			cursor.moveToFirst();
			String filePath = cursor.getString(cursor.getColumnIndex(queryColumns[0]));
			String mimeType = cursor.getString(cursor.getColumnIndex(queryColumns[1]));
			String fileName = cursor.getString(cursor.getColumnIndex(queryColumns[2]));
			cursor.close();
			
	        InputStream in = new BufferedInputStream(new FileInputStream(filePath)); 
	        FileData data = new FileData(EDAMUtil.hash(in), new File(filePath));
	        in.close();
	        
	        ResourceAttributes atributos = new ResourceAttributes();
	        atributos.setFileName(fileName);
	        
	        Resource resource = new Resource();
	        resource.setData(data);
	        resource.setMime(mimeType);
	        resource.setAttributes(atributos);
	        
	        nota.addToResources(resource);
	        
	        return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	// Retorna a nota associada à corrida
	public Note getNota() {
		return nota;
	}
	
	// Apaga o arquivo temporário
	// Não esqueça de fazer isso quando criar um arquivo!!!
	public void limpar() {
		if (arquivo != null) {
			arquivo.delete();
			arquivo = null;
		}
	}
	
	// Compila os dados para a nota e manda para o Evernote (atualiza o XML, o anexo .kml e o arquivo no SD)
	// Retorna true se a exportação ocorreu com sucesso
	public boolean salvar() {
		try {
			// Lê o documento
			Document doc = ler();
			
			Element antigoSpan = null;
			NodeList spans = doc.getElementsByTagName("span");
			for (int i=0; i<spans.getLength(); i++) {
				Element el = (Element)spans.item(i);
				String name = el.getAttribute("style");
				if (name.startsWith("hack-calorias"))
					el.setTextContent(Integer.toString(calorias));
				else if (name.startsWith("hack-suor"))
					el.setTextContent(Integer.toString(suor));
				else if (name.startsWith("hack-distancia"))
					el.setTextContent(Double.toString(distancia));
				else if (name.startsWith("hack-tempoH"))
					el.setTextContent(Integer.toString(tempo/3600));
				else if (name.startsWith("hack-tempoM"))
					el.setTextContent(Integer.toString((tempo/60)%60));
				else if (name.startsWith("hack-tempoS"))
					el.setTextContent(Integer.toString(tempo%60));
				else if (name.startsWith("hack-velocidade"))
					el.setTextContent(Double.toString(velocidade));
				else if (name.startsWith("hack-clima"))
					el.setTextContent(Integer.toString(clima));
				else if (name.startsWith("hack-comentarios"))
					el.setTextContent(comentarios);
				else if (name.startsWith("hack-imagens"))
					antigoSpan = el;
				else if (name.startsWith("hack-data"))
					el.setTextContent(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(data));
			}
			
			// Atualiza a imagem
			NodeList imgs = doc.getElementsByTagName("img");
			for (int i=0; i<imgs.getLength(); i++) {
				Element el = (Element)imgs.item(i);
				if (el.getAttribute("style").startsWith("hack-grafico")) {
					el.setAttribute("src", graficoUrl);
					break;
				}
			}
			
			// Atualiza a lista de imagens
			Element novoSpan = doc.createElement("span");
			novoSpan.setAttribute("style", "hack-imagens");
			antigoSpan.getParentNode().replaceChild(novoSpan, antigoSpan);
			List<Resource> anexos = nota.getResources();
			for (int i=0; i<nota.getResourcesSize(); i++)
				if (anexos.get(i).getMime().startsWith("image/")) {
					Element img = doc.createElement("en-media");
					img.setAttribute("width", "80%");
					img.setAttribute("type", anexos.get(i).getMime());
					img.setAttribute("hash", EDAMUtil.bytesToHex(anexos.get(i).getData().getBodyHash()));
					novoSpan.appendChild(img);
				}
			
			if (rota.length() > 0) {
				// Atualiza os dados relativos ao KML
				
				// Pega o arquivo .kml
				Resource kml = getKml();
				if (kml == null) {
					kml = new Resource();
					kml.setMime(KML_MIME_TYPE);
					ResourceAttributes atributos = new ResourceAttributes();
					atributos.setFileName("rota.kml");
					kml.setAttributes(atributos);
					nota.addToResources(kml);
				}
				
				// Atualiza o arquivo .kml
				Data data = new Data();
				byte[] body = rota.getBytes();
				data.setBody(body);
				data.setBodyHash(EDAMUtil.hash(body));
				data.setSize(body.length);
				kml.setData(data);
				
				// Atualiza o link
				NodeList nos = doc.getElementsByTagName("en-media");
				Element enMediaKml = null;
				for (int i=0; i<nos.getLength(); i++) {
					Element el = (Element)nos.item(i);
					if (el.getAttribute("style").startsWith("hack-linkKml")) {
						enMediaKml = el;
					}
				}
				enMediaKml.setAttribute("hash", EDAMUtil.bytesToHex(kml.getData().getBodyHash()));
				
				// Atualiza no SD
				if (arquivo != null)
					arquivo.write(rota);
			}
			
			// Salva o novo XML na nota
			nota.setContent(XML2String(doc));
			new SDCard("hibonit", "meuHtml.html").write(toHTML());
			
			// Manda para o Evernote
			if (nota.isSetGuid())
				nota = Evernote.getNoteStore().updateNote(Evernote.getAuthToken(), nota);
			else
				nota = Evernote.getNoteStore().createNote(Evernote.getAuthToken(), nota);
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// Lê o conteúdo da nota para um documento
	private Document ler() throws Exception {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		String conteudo = nota.getContent();
		if (conteudo == null)
			conteudo = Evernote.getNoteStore().getNoteContent(Evernote.getAuthToken(), nota.getGuid()); 
		InputSource source = new InputSource(new StringReader(nota.getContent()));
		return builder.parse(source);
	}
	
	// Retorna o recurso referente ao arquivo .kml
	// Retorna null caso não encontre
	private Resource getKml() {
		int max = nota.getResourcesSize();
		List<Resource> anexos = nota.getResources();
		Resource kml = null;
		for (int i=0; i<max; i++)
			if (anexos.get(i).getMime().equalsIgnoreCase(KML_MIME_TYPE))
				kml = anexos.get(i);
		return kml;
	}
	
	// Transforma de XML para String
	private String XML2String(Document doc) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			String str = writer.toString();
			str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"+str.substring(38);
			return str;
		} catch (Exception e) {
			return "";
		}
	}
	
	// Cria um arquivo temporário para salvar o .kml que será enviado ao maps
	private SDCard criarArquivo() {
		return new SDCard("hibonit", "rand"+String.valueOf(new Date().getTime())+".kml");
	}
	
	// Atua como um Node.setTagName(String novo)
	// Copia todos os atributos e nós filho e substitui no documento
	private void setTagName(Element antigo, String tagName) {
		// Cria o novo elemento
		Element novo;
		int i;
		novo = antigo.getOwnerDocument().createElement(tagName);
		
		// Copia os atributos
		NamedNodeMap atributos = antigo.getAttributes();
		for (i=0; i<atributos.getLength(); i++) {
			Node item = atributos.item(i);
			novo.setAttribute(item.getNodeName(), item.getNodeValue());
		}
		
		// Copia os nós filhos
		NodeList filhos = antigo.getChildNodes();
		for (i=0; i<filhos.getLength(); i++)
			novo.appendChild(filhos.item(i));
		
		// Substitui
		antigo.getParentNode().replaceChild(novo, antigo);
	}
}
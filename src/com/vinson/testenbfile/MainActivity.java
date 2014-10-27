package com.vinson.testenbfile;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private static final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp/test1_1.zip";

	private static final String SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IClass";
	private static final String SAVE_FILE =
			Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp/save.zip";

	private static final String XML_FILE =
			Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp/info.xml";
	private Button mLoadButton;
	private Button mSaveButton;
	private Button mCreateButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mLoadButton = (Button) findViewById(R.id.load_button);
		mLoadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View pView) {
				try {
					readZip(PATH);
				} catch (IOException e) {
					Toast.makeText(MainActivity.this, "got error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		});

		mSaveButton = (Button) findViewById(R.id.save_button);
		mSaveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				onSaveClick();
			}
		});

		mCreateButton = (Button) findViewById(R.id.create_button);
		mCreateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ArrayList<Message> mesages = new ArrayList<Message>();
				for (int i = 0; i < 5; ++i) {
					Message message = new Message();
					mesages.add(message);
				}

				createXmlFile(mesages);
			}
		});
	}

	private void readZip(String path) throws IOException {
		ZipFile zipFile = new ZipFile(path);
		for (Enumeration<? extends ZipEntry> enu = zipFile.entries(); enu.hasMoreElements();) {
			ZipEntry zipEntry = enu.nextElement();
			String name = zipEntry.getName();
			Log.v(TAG, "got fileName:" + name);
			if ("Document.xml".equals(name)) {
				readFileContent(zipFile.getInputStream(zipEntry));
			}
		}
	}

	private void readFileContent(InputStream inputStream) throws IOException {
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bReader = new BufferedReader(inputStreamReader);
		String line = null;
		while ((line = bReader.readLine()) != null) {
			Log.v(TAG, line);
		}
		bReader.close();
	}

	private void onSaveClick() {
		ZipOutputStream zipOutputStream = null;
		try {
			File destFile = new File(SAVE_FILE);
			if (destFile.exists()) {
				destFile.delete();
			}
			destFile.createNewFile();

			zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(SAVE_FILE)));
			File dir = new File(SAVE_PATH);
			saveFile("", zipOutputStream, dir);

			Toast.makeText(MainActivity.this, "Save Zip file successfully", Toast.LENGTH_SHORT).show();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != zipOutputStream) {
				try {
					zipOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void saveFile(String base, ZipOutputStream zipOutputStream, File dir) throws IOException {

		File[] fileNames = dir.listFiles();
		for (File file : fileNames) {
			Log.v(TAG, "ziping file:" + file.getAbsolutePath());
			if (file.isDirectory()) {
				saveFile(base + file.getName() + "/", zipOutputStream, file);
				continue;
			}

			String fileName = file.getName();
			FileInputStream fileInputStream = new FileInputStream(file);
			byte[] buf = new byte[1024];
			int count = -1;

			zipOutputStream.putNextEntry(new ZipEntry(base + fileName));
			while ((count = fileInputStream.read(buf)) != -1) {
				zipOutputStream.write(buf, 0, count);
			}

			zipOutputStream.flush();
			zipOutputStream.closeEntry();

			fileInputStream.close();
		}
	}

	private void createXmlFile(List<Message> messages) {

		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", "messages");
			serializer.attribute("", "number", String.valueOf(messages.size()));
			for (Message msg : messages) {
				serializer.startTag("", "message");
				serializer.attribute("", "date", msg.getDate());
				serializer.startTag("", "title");
				serializer.text(msg.getTitle());
				serializer.endTag("", "title");
				serializer.startTag("", "url");
				serializer.text(msg.getLink());
				serializer.endTag("", "url");
				serializer.startTag("", "body");
				serializer.text(msg.getDescription());
				serializer.endTag("", "body");
				serializer.endTag("", "message");
			}
			serializer.endTag("", "messages");
			serializer.endDocument();

			File file = new File(XML_FILE);
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			FileOutputStream fouFileOutputStream = new FileOutputStream(file);
			fouFileOutputStream.write(format(writer.toString()).getBytes());
			fouFileOutputStream.flush();
			fouFileOutputStream.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public String format(String unformattedXml) {
		// Instantiate transformer input
		Source xmlInput = new StreamSource(new StringReader(unformattedXml));
		StreamResult xmlOutput = new StreamResult(new StringWriter());

		// Configure transformer
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(xmlInput, xmlOutput);

			// Print the pretty XML
			System.out.println(xmlOutput.getWriter().toString());
			return xmlOutput.getWriter().toString();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	private class Message {
		public String getDate() {
			return "1234";
		}

		public String getDescription() {
			return "3213123";
		}

		public String getLink() {
			return "rwrefwere";
		}

		public String getTitle() {
			return "fdsfsd";
		}
	}
}

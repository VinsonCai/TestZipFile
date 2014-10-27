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
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
	private Button mLoadButton;
	private Button mSaveButton;

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

}

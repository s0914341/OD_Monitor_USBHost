package od_monitor.app.file;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class FileOperateBmp extends FileOperateByteArray {
	private String Tag = "FileOperateBmp";
	protected String file_extension = "png";
	protected SimpleDateFormat file_date = new SimpleDateFormat("yyyyMMdd_HHmmss");
	
	public FileOperateBmp(String dir_name, String file_name, String file_extension) {
		super(dir_name, file_name, false);
		this.file_extension = file_extension;
		// TODO Auto-generated constructor stub
	}
	
	/* file naming format logyyyymmdd-hhmmss.txt*/
	@Override
	public String generate_filename() {
	    String filename;
		filename = CreateFileName + file_date.format(new Date()) + "." + file_extension;
		Log.d(Tag, filename);
		return filename;
	}
		
	@Override
	public String generate_filename_no_date() {
		String filename;
		filename = CreateFileName + "." + file_extension;
		Log.d(Tag, filename);
		return filename;
	}

	public void write_file(Bitmap bitmap, CompressFormat format, int quality) {
		if (fos != null) {
			try {
			    bitmap.compress(format, quality, fos);
			} catch (Exception e) {
			    e.printStackTrace();
			}
		}
	}
}

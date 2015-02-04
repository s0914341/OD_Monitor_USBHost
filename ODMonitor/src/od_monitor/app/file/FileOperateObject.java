package od_monitor.app.file;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;

public class FileOperateObject extends FileOperateByteArray {
	private String Tag = "FileOperateObject";
	protected String file_extension = ".ojt";
	
	public FileOperateObject(String dir_name, String file_name) {
		super(dir_name, file_name, false);
		set_file_extension(file_extension);
		// TODO Auto-generated constructor stub
	}

	public void write_file(Object object) {
		if (fos != null) {
			try {
				ObjectOutputStream os = new ObjectOutputStream(fos);
				os.writeObject(object);
				os.close();
				fos.flush();
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public Object read_file_object() throws ClassNotFoundException, IOException {
		Object object = null;
		if (fis != null) {
			ObjectInputStream is = new ObjectInputStream(fis);
			object = is.readObject();
			is.close();
			fis.close();
		}
		
		return object;
	}
}

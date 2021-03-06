import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Time;

public class Client {
	public static Socket socket;
	public InputStream input;
	public OutputStream output;
	byte[] buffer;

	public Client(InetAddress address, int port) throws IOException {
		socket = new Socket(address, port);	
		input = socket.getInputStream();
		output = socket.getOutputStream();
		buffer = new byte[512];
	}

	/* Sends Echo command and returns current time */
	public void sendEcho(long nb) throws IOException {
		output.write(("/echo " + nb).getBytes());
		output.flush();
		/* read "echo :\n" message */
		for(int i=0;i<7;++i)
			input.read();
	}


	public void sendBytes(int nb) throws IOException {
		this.sendEcho(nb);
		byte[] array = new byte[nb];
		output.write(array);
		output.flush();
		for (int i = 0; i < nb; ++i)
			input.read();
	}

	private void waitForString(String expected) throws IOException {
		String s = new String(buffer);
		while (!s.contains(expected)) {
			int size = input.read(buffer);
			s = new String(buffer, 0, size);
		}
	}

	/* Closes the connection of client */
	public void close() throws IOException {
		socket.close();
	}

	public static void sendBytes(int nb, int pas) throws IOException {
		for (int i = pas; i <= nb; i += pas) {
			//System.out.println(" "+i);
			Client client = new Client(InetAddress.getLocalHost(), 7654);
			client.sendBytes(pas);
			client.close();
		}
		if(nb%pas != 0){
			Client client = new Client(InetAddress.getLocalHost(), 7654);
			client.sendBytes(nb%pas);
			client.close();
		}			
	}

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		int begin = 1;
		int end = 10000;
		int step = 100;
		int size = 10000;
		int nbExp = 10;
		long results [] [] = new long [(end-begin+1)/step] [nbExp];
		for (int i = 0; i < args.length; ++i) {
			switch (args[i]) {
			case "-begin":
				begin = Integer.parseInt(args[i + 1]);
				break;
			case "-end":
				end = Integer.parseInt(args[i + 1]);
				break;
			case "-step":
				step = Integer.parseInt(args[i + 1]);
			case "-size":
				size = Integer.parseInt(args[i + 1]);
				break;
			default:
				break;
			}
		}
		for(int j=0;j<nbExp;++j){
			for (int i = begin; i < end; i += step) {			
				long beginTime = System.currentTimeMillis();
				Client.sendBytes(size, i);
				long endTime = System.currentTimeMillis();
				results [ (i-begin) / step ] [j] = (endTime - beginTime);
			}
			
		}
		
		for(int j=0;j<((end-begin)/step);++j){
			long somme = 0;
			for(int i=0;i<nbExp;++i)
				somme += results[j][i];
			long time = somme / nbExp;
			System.out.println((begin + (j*step)) + "\t" + ((size / (time+1)))*1000/1024);
		}
	}
}

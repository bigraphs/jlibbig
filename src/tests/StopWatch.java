package tests;

public class StopWatch {
	private long _start = -1;
	private long _ellapsed = 0;

	public static StopWatch startNew() {
		StopWatch s = new StopWatch();
		s.start();
		return s;
	}

	public long getStartTime() {
		return _start;
	}

	public long getEllapsedNano() {
		if (_start > -1) {
			_ellapsed += System.nanoTime() - _start;
			_start = System.nanoTime();
		}
		return _ellapsed;
	}

	public long getEllapsedMillis() {
		return getEllapsedNano() / 1000000;
	}

	public boolean isRunning() {
		return _start == -1;
	}

	public void start() {
		if (_start == -1) {
			_start = System.nanoTime();
		}
	}

	public void stop() {
		if (_start > -1) {
			_ellapsed += System.nanoTime() - _start;
			_start = -1;
		}
	}

	public void reset() {
		_ellapsed = 0;
		if (_start > -1) {
			_start = System.nanoTime();
		}
	}

}

package com.dingok.movie.seatmap.seatMapView;

/**
 * Created by yanzewei on 2017/2/15.
 */

public class SeatView {

    private int _index;
    private int _x;
    private int _y;
    private int _state;
    private String _row;
    private String _col;
    private String _area;

    SeatView(int index, int x, int y, int state, String row, String col, String area) {
        this._index = index;
        this._x = x;
        this._y = y;
        this._state = state;
        this._row = row;
        this._col = col;
        this._area = area;
    }

    public int index() {
        return _index;
    }

    public int x() {
        return _x;
    }

    public int y() {
        return _y;
    }

    public int state() {
        return _state;
    }

    public String row() {
        return _row;
    }

    public String col() {
        return _col;
    }

    public String area() {
        return _area;
    }

    public void state(int n) {
        _state = n;
    }

}

/*
 * Copyright (c) 2018.
 *
 * This file is part of av.
 *
 * av is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * av is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with av.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avbot.requests;

import com.avbot.av;
import okhttp3.ResponseBody;

import java.io.IOException;

public class Response {
    private final okhttp3.Response response;

    public Response(okhttp3.Response response) {
        this.response = response;
    }

    public okhttp3.Response getResponse() {
        return response;
    }

    public Object toService(Class<?> clazz) {
        return av.gson.fromJson(toString(), clazz);
    }

    @Override
    public String toString() {
        try {
            try (ResponseBody body = response.body()) {
                if (body != null) {
                    return body.string();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

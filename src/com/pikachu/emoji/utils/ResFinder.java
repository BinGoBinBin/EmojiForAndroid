/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Umeng, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.pikachu.emoji.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

/**
 * @author BinGoBinBin 
 */
public abstract class ResFinder {
    /**
     * @功能描述 : 资源类型
     */
    public enum ResType {
        LAYOUT {
            @Override
            public String toString() {
                return "layout";
            }
        },
        ID {
            @Override
            public String toString() {
                return "id";
            }
        },
        DRAWABLE {
            @Override
            public String toString() {
                return "drawable";
            }
        },
        STYLE {
            @Override
            public String toString() {
                return "style";
            }
        },
        STYLEABLE {
            @Override
            public String toString() {
                return "styleable";
            }
        },
        STRING {
            @Override
            public String toString() {
                return "string";
            }
        },
        COLOR {
            @Override
            public String toString() {
                return "color";
            }
        },
        DIMEN {
            @Override
            public String toString() {
                return "dimen";
            }
        },
        RAW {
            @Override
            public String toString() {
                return "raw";
            }
        },
        ANIM {
            @Override
            public String toString() {
                return "anim";
            }
        },
        ARRAY {
            @Override
            public String toString() {
                return "array";
            }
        }
    }

    /**
     * 资源缓存map
     */
    private static Map<ResItem, Integer> mResourcesCache = new HashMap<ResItem, Integer>();
    private static final int NOT_FOUND = -1;
    private static Context mContext;
    /** 开发者App的包名 */
    private static String mPackageName = "";

    /**
     * 初始化Context,必须在初始化CommunitySDK时初始化
     * 
     * @param mContext 必须是ApplicationContext
     */
    public static void initContext(Context context) {
        mContext = context;
        if (mContext == null) {
            throw new NullPointerException("初始化ResFinder失败，传递的Context为空.");
        }

        mPackageName = mContext.getPackageName();
    }

    /**
     * 开发者在外部手动设置包名。使用场景：开发者在打包的过程中修改了包名，比如利用aapt方式（ {@link http
     * ://www.piwai.info/renaming-android-manifest-package/}），
     * 由于sdk在运行时获取资源id，此时将出现无法找到资源的情况。 </br>
     * 
     * @param packageName
     */
    public static void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    public static Context getApplicationContext() {
        return mContext;
    }

    /**
     * 根据资源类型和名称查找资源id,首先从缓存中读取,有缓存则直接返回,否则从资源目录中查找,找到后再缓存到map中.
     * 
     * @param mContext 上下文
     * @param type 资源类型
     * @param name 资源名称
     * @return
     */
    public static int getResourceId(ResType type, String name) {

        ResItem item = new ResItem(type, name);
        // 从缓存中读取
        int rid = getIdFromCache(item);
        if (rid != NOT_FOUND) {
            return rid;
        }
        // 没有缓存该资源,那么直接查找
        Resources resources = mContext.getResources();
        rid = resources.getIdentifier(name, type.toString(), mPackageName);
        if (rid <= 0) {
            throw new RuntimeException("获取资源ID失败:(packageName=" + mPackageName
                    + " type=" + type + " name=" + name + ", 请确保的res/" + type.toString()
                    + "目录中含有该资源");
        }

        // 解析成功后缓存到map中
        mResourcesCache.put(item, rid);
        return rid;
    }

    private static int getIdFromCache(ResItem item) {
        return mResourcesCache.containsKey(item) ? mResourcesCache.get(item) : NOT_FOUND;
    }

    /**
     * 获取字符串
     * 
     * @param mContext 上下文
     * @param name 字符串的名字
     * @return
     */
    public static String getString(String name) {
        int resId = getResourceId(ResType.STRING, name);
        return mContext.getString(resId);
    }

    public static int getLayout(String name) {
        return getResourceId(ResType.LAYOUT, name);
    }

    public static int getColor(String name) {
        return mContext.getResources().getColor(getResourceId(ResType.COLOR, name));
    }

    public static int getId(String name) {
        return getResourceId(ResType.ID, name);
    }

    public static int getStyle(String name) {
        return getResourceId(ResType.STYLE, name);
    }

    public static int getStyleableId(String name) {
        return getResourceId(ResType.STYLEABLE, name);
    }

    public static int[] getStyleableArrts(String name) {
        // return mContext.getResources().getIntArray(getStyleableId(name));
        return getResourceDeclareStyleableIntArray(mContext, name);
    }

    public static float getDimen(String name) {
        return mContext.getResources().getDimension(getResourceId(ResType.DIMEN, name));
    }

    @SuppressWarnings("deprecation")
    public static Drawable getDrawable(String name) {
        return mContext.getResources().getDrawable(getResourceId(ResType.DRAWABLE, name));
    }

    /**
     * 资源项,代表某个资源,以资源类型和资源名作为唯一的标识.
     * 
     * @author mrsimple
     */
    public static class ResItem {
        public ResType mType;
        public String mName;

        public ResItem(ResType type, String name) {
            this.mType = type;
            this.mName = name;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((mName == null) ? 0 : mName.hashCode());
            result = prime * result + ((mType == null) ? 0 : mType.toString().hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ResItem other = (ResItem) obj;
            if (mName == null) {
                if (other.mName != null)
                    return false;
            } else if (!mName.equals(other.mName))
                return false;
            if (mType != other.mType)
                return false;
            return true;
        }
    } // end of SocializeResource

    private static final int[] getResourceDeclareStyleableIntArray(Context context, String name) {
        try {
            // use reflection to access the resource class
            Field[] fields2 = Class.forName(context.getPackageName() + ".R$styleable").getFields();

            // browse all fields
            for (Field f : fields2) {
                // pick matching field
                if (f.getName().equals(name)) {
                    // return as int array
                    int[] ret = (int[]) f.get(null);
                    return ret;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return null;
    }
}

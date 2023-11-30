package com.jflove.android.rewrite;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.jflove.android.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: tanjun
 * @date: 2023/11/27 2:52 PM
 * @desc: 折叠列表,2层折叠, 通用的类
 */
public class MenuBaseExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private View.OnClickListener onClickListener;//菜单点击事件回调

    private View.OnLongClickListener onLongClickListener;//长按事件回调
    /**
     * json 结构
     * [{name:'',id:0,code:'',child:[]}]
     */
    private List<Map<String,Object>> list;
    /**
     * 以控件ID的方式存储数据
     * 这样控件在触发点击事件的时候,可以通过控件ID找到对应的数据
     */
    private Map<Integer,Map<String,Object>> mapData = new HashMap<>();

    public MenuBaseExpandableListAdapter(Context context,List<Map<String,Object>> list) {
        this.context = context;
        this.list = list;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    /**
     * 获取组的数目
     *
     * @return 返回一级列表组的数量
     */
    @Override
    public int getGroupCount() {
        return list == null ? 0 : list.size();
    }

    /**
     * 获取指定组中的子节点数量
     *
     * @param groupPosition 子元素组所在的位置
     * @return 返回指定组中的子数量
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        List child = (List)list.get(groupPosition).get("child");
        return child == null ? 0 : child.size();
    }

    /**
     * 获取与给定组相关联的对象
     *
     * @param groupPosition 子元素组所在的位置
     * @return 返回指定组的子数据
     */
    @Override
    public Object getGroup(int groupPosition) {
        return (String)list.get(groupPosition).get("name");
    }


    /**
     * 获取与给定组中的给定子元素关联的数据
     *
     * @param groupPosition 子元素组所在的位置
     * @param childPosition 子元素的位置
     * @return 返回子元素的对象
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        List child = (List)list.get(groupPosition).get("child");
        return child.get(childPosition);
    }

    /**
     * 获取组在给定位置的ID（唯一的）
     *
     * @param groupPosition 子元素组所在的位置
     * @return 返回关联组ID
     */
    @Override
    public long getGroupId(int groupPosition) {
        return (long)list.get(groupPosition).get("id");
    }


    /**
     * 获取给定组中给定子元素的ID(唯一的)
     *
     * @param groupPosition 子元素组所在的位置
     * @param childPosition 子元素的位置
     * @return 返回子元素关联的ID
     */
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        List<Map> child = (List<Map>)list.get(groupPosition).get("child");
        return (long) child.get(childPosition).get("id");
    }

    /**
     * @return 确定id 是否总是指向同一个对象
     */
    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * @return 返回指定组的对应的视图 （一级列表样式）
     */
    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.listview_item, null);//必须每次重建对象,否则会发生刷新菜单的时候,菜单事件等信息不刷新,仅仅是改了个名称
        ParentHolder parentHolder = new ParentHolder();
        parentHolder.tvParent = convertView.findViewById(R.id.listView_menu_item);
        parentHolder.tvParent.setText((CharSequence) getGroup(groupPosition));
        parentHolder.tvParent.setId((int)getGroupId(groupPosition));
        mapData.put(parentHolder.tvParent.getId(),list.get(groupPosition));
        if(getChildrenCount(groupPosition) == 0 && onClickListener != null) {//没有子菜单,则给当前按钮增加事件回调
            parentHolder.tvParent.setOnClickListener(onClickListener);
        }
        if(onLongClickListener != null){
            parentHolder.tvParent.setOnLongClickListener(onLongClickListener);
        }
        convertView.setTag(parentHolder);
        return convertView;
    }

    /**
     * @return 返回指定位置对应子视图的视图
     */
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.listview_item_child, null);//必须每次重建对象,否则会发生刷新菜单的时候,菜单事件等信息不刷新,仅仅是改了个名称
        ChildrenHolder childrenHolder = new ChildrenHolder();
        childrenHolder.tvChild = convertView.findViewById(R.id.listView_menu_item_child);
        Map map = ((Map)getChild(groupPosition,childPosition));
        childrenHolder.tvChild.setText((CharSequence) map.get("name"));
        childrenHolder.tvChild.setId((int)getChildId(groupPosition,childPosition));
        mapData.put(childrenHolder.tvChild.getId(),map);
        List child = (List) map.get("child");
        if((child == null || child.size() == 0) && onClickListener != null) {//没有子菜单,则给当前按钮增加事件回调
            childrenHolder.tvChild.setOnClickListener(onClickListener);
        }
        if(onLongClickListener != null){
            childrenHolder.tvChild.setOnLongClickListener(onLongClickListener);
        }
        convertView.setTag(childrenHolder);
        return convertView;
    }

    /**
     * 指定位置的子元素是否可选
     *
     * @param groupPosition 子元素组所在的位置
     * @param childPosition 子元素的位置
     * @return 返回是否可选
     */

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        if(getChildrenCount(groupPosition) == 0){
            return false;
        }
        Map map = (Map) getChild(groupPosition,childPosition);
        List child = (List) map.get("child");
        return child != null && child.size() != 0;
    }

    /**
     * 用于刷新更新后的数据
     * @param list
     */
    public void reFreshData(List<Map<String,Object>> list) {
        mapData.clear();
        this.list = list;
        notifyDataSetChanged();
    }

    class ParentHolder {
        TextView tvParent;
    }


    class ChildrenHolder {
        TextView tvChild;
    }

    /**
     * 根据控件ID获取对应的数据
     * @param id
     * @return
     */
    public Map<String,Object> getDataById(int id) {
        return mapData.get(id);
    }

    /**
     * 根据控件ID移除这行数据
     * @param id
     */
    public void removeRowById(int id){
        Map r = mapData.remove(id);
        list.remove(r);
        notifyDataSetChanged();
    }
}

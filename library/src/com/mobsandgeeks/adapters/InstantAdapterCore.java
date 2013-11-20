/* 
 * Copyright © 2013 Mobs & Geeks
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobsandgeeks.adapters;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * {@link InstantAdapterCore} does all the heavy lifting behind the scenes for {@link InstantAdapter} and {@link InstantCursorAdapter}. We use composition instead of inheritance because
 * {@link InstantAdapter} already extends the {@link ArrayAdapter}.
 * 
 * @author Ragunath Jawahar <rj@mobsandgeeks.com>
 * 
 * @param <T>
 *            The model that is being backed by the {@link InstantAdapter} or {@link InstantCursorAdapter}.
 */
class InstantAdapterCore<T> extends InstantLayoutAdapterCore<T> {

	// Debug
	static final String LOG_TAG = InstantAdapterCore.class.getSimpleName();
	static final boolean DEBUG = false;

	private ListAdapter mAdapter;
	private int mLayoutResourceId;
	private LayoutInflater mLayoutInflater;

	/**
	 * Constructs a new {@link InstantAdapterCore} for your {@link InstantAdapter} and {@link InstantCursorAdapter}.
	 * 
	 * @param context
	 *            The {@link Context} to use.
	 * @param adapter
	 *            The adapter using this instance of {@link InstantAdapterCore}.
	 * @param layoutResourceId
	 *            The resource id of your XML layout.
	 * @param dataType
	 *            The data type backed by your adapter.
	 * 
	 * @throws IllegalArgumentException
	 *             If {@code context} is null or {@code layoutResourceId} is invalid or {@code type} is {@code null}.
	 */
	public InstantAdapterCore(final Context context, final ListAdapter adapter, final int layoutResourceId, final Class<?> dataType) {
		super(context, dataType);
		if (layoutResourceId == View.NO_ID || layoutResourceId == 0) {
			throw new IllegalArgumentException("Invalid 'layoutResourceId', please check again.");
		}

		mLayoutResourceId = layoutResourceId;
		mLayoutInflater = LayoutInflater.from(context);
	}

	/**
	 * Method binds a POJO to the inflated View.
	 * 
	 * @param parent
	 *            The {@link View}'s parent, usually an {@link AdapterView} such as a {@link ListView}.
	 * @param view
	 *            The associated view.
	 * @param instance
	 *            Instance backed by the adapter at the given position.
	 * @param position
	 *            The list item's position.
	 */
	@Override
	public final void bindToView(final ViewGroup parent, final View view, final T instance, final int position) {
		SparseArray<Holder> holders = (SparseArray<Holder>) view.getTag(mLayoutResourceId);
		updateAnnotatedViews(holders, view, instance, position);
		executeViewHandlers(holders, parent, view, instance, position);
	}

	/**
	 * Create a new view by inflating the associated XML layout.
	 * 
	 * @param context
	 *            The {@link Context} to use.
	 * @param parent
	 *            The inflated view's parent.
	 * @return The {@link View} that was inflated from the layout.
	 */
	public final View createNewView(final Context context, final ViewGroup parent) {
		View view = mLayoutInflater.inflate(mLayoutResourceId, parent, false);
		SparseArray<com.mobsandgeeks.adapters.InstantLayoutAdapterCore.Holder> holders = setupForView(view);
		view.setTag(mLayoutResourceId, holders);
		return view;
	}

	@Override
	protected void handleViewInternal(ViewHandler<T> viewHandler, final View parent, View view, final T instance, final int position) {
		viewHandler.handleView(mAdapter, parent, view, instance, position);
	}

	@Override
	protected void executeViewHandlers(final SparseArray<Holder> holders, final View parent, final View view, final T instance, final int position) {
		int nViewHandlers = mViewHandlers.size();
		for (int i = 0; i < nViewHandlers; i++) {
			int viewId = mViewHandlers.keyAt(i);
			ViewHandler<T> viewHandler = mViewHandlers.get(viewId);

			if (viewHandler == null)
				continue;

			if (viewId == mLayoutResourceId) {
				handleViewInternal(viewHandler, parent, view, instance, position);
			} else {
				Holder holder = holders.get(viewId);
				View viewWithId = null;
				if (holder != null) {
					viewWithId = holder.view;
				} else {
					viewWithId = view.findViewById(viewId);
					holders.append(viewId, new Holder(viewWithId, null));
				}
				handleViewInternal(viewHandler, parent, viewWithId, instance, position);
			}
		}
	}
}

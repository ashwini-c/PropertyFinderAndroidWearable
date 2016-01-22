package org.stevej.android.propertyfinder.utils;

import java.util.Stack;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup;

public class CustomAnimator {
	private static final String				TAG				= "CustomAnimator";

	private static Stack<AnimationEntry> animation_stack = new Stack<AnimationEntry>();

	public static final int DIRECTION_LEFT = 1;
	public static final int DIRECTION_RIGHT = -1;
	public static final int DIRECTION_UP = 2;
	public static final int DIRECTION_DOWN = -2;

	static class AnimationEntry {
		View in;
		View out;
		int direction;
		long duration;
	}

	public static boolean hasHistory() {
		return !animation_stack.empty();
	}

	public static void reversePrevious() {
		if (!animation_stack.empty()) {
			AnimationEntry entry = animation_stack.pop();
			slide(entry.out, entry.in, -entry.direction, entry.duration, false);
		}
	}

	public static void clearHistory() {
		animation_stack.clear();
	}

	public static void slide(final View in, View out, final int direction, long duration) {
		slide(in, out, direction, duration, true);
	}

	private static void slide(final View in, final View out, final int direction, final long duration, final boolean save) {

		ViewGroup in_parent = (ViewGroup) in.getParent();
		ViewGroup out_parent = (ViewGroup) out.getParent();

		if (!in_parent.equals(out_parent)) {
			return;
		}

		int parent_width = in_parent.getWidth();
		int parent_height = in_parent.getHeight();

		ObjectAnimator slide_out;
		ObjectAnimator slide_in;

		switch (direction) {
		case DIRECTION_LEFT:
		default:
			slide_in = ObjectAnimator.ofFloat(in, "translationX", parent_width, 0);
			slide_out = ObjectAnimator.ofFloat(out, "translationX", 0, -out.getWidth());
			break;
		case DIRECTION_RIGHT:
			slide_in = ObjectAnimator.ofFloat(in, "translationX", -out.getWidth(), 0);
			slide_out = ObjectAnimator.ofFloat(out, "translationX", 0, parent_width);
			break;
		case DIRECTION_UP:
			slide_in = ObjectAnimator.ofFloat(in, "translationY", parent_height, 0);
			slide_out = ObjectAnimator.ofFloat(out, "translationY", 0, -out.getHeight());
			break;
		case DIRECTION_DOWN:
			slide_in = ObjectAnimator.ofFloat(in, "translationY", -out.getHeight(), 0);
			slide_out = ObjectAnimator.ofFloat(out, "translationY", 0, parent_height);
			break;
		}

		AnimatorSet animations = new AnimatorSet();
		animations.setDuration(duration);
		animations.playTogether(slide_in, slide_out);
		animations.addListener(new AnimatorListener() {

			@Override
			public void onAnimationCancel(Animator arg0) {
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				out.setVisibility(View.INVISIBLE);
				if (save) {
					AnimationEntry ae = new AnimationEntry();
					ae.in = in;
					ae.out = out;
					ae.direction = direction;
					ae.duration = duration;
					animation_stack.push(ae);
				}
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
			}

			@Override
			public void onAnimationStart(Animator arg0) {
				in.setVisibility(View.VISIBLE);
			}
		});
		animations.start();
	}
}

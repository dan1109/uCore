package io.anuke.ucore.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.Timer.Task;
import io.anuke.ucore.function.DelayRun;
import io.anuke.ucore.util.Pooling;

public class Timers{
    /** Time resets after 5 hours due to percision issues. */
    private static final float maxValue = 1080000;

    private static float time;
    private static IntFloatMap timers = new IntFloatMap();
    private static DelayedRemovalArray<DelayRun> runs = new DelayedRemovalArray<>();
    private static LongArray marks = new LongArray();
    private static DeltaProvider deltaimpl = () -> Math.min(Gdx.graphics.getDeltaTime() * 60f, 3f);

    public static synchronized void run(float delay, Runnable r){
        DelayRun run = Pooling.obtain(DelayRun.class);
        run.finish = r;
        run.delay = delay;
        runs.add(run);
    }

    public static synchronized void runTask(float delay, Runnable r){
        Timer.schedule(new Task(){
            @Override
            public void run(){
                r.run();
            }
        }, delay / 60f);
    }

    public static void runFor(float duration, Runnable r){
        DelayRun run = Pooling.obtain(DelayRun.class);
        run.run = r;
        run.delay = duration;
        runs.add(run);
    }

    public static void runFor(float duration, Runnable r, Runnable finish){
        DelayRun run = Pooling.obtain(DelayRun.class);
        run.run = r;
        run.delay = duration;
        run.finish = finish;
        runs.add(run);
    }

    public static synchronized void reset(Object o, String label, float duration){
        timers.put(hash(o, label), time - duration);
    }

    public static synchronized void clear(){
        runs.clear();
        timers.clear();
    }

    public static synchronized float getTime(Object object, String label){
        return time() - timers.get(hash(object, label), 0f);
    }

    public static float getTime(String label){
        return time() - timers.get(hash(label), 0f);
    }

    public static boolean get(String label, float frames){
        return get(hash(label), frames);
    }

    public static boolean get(Object object, String label, float frames){
        return get(hash(object, label), frames);
    }

    public static synchronized boolean get(int hash, float frames){
        if(timers.containsKey(hash)){
            float value = timers.get(hash, time);

            if(time - value > frames || time < value){ //fix time travel too
                timers.put(hash, time);
                return true;
            }else{
                return false;
            }
        }else{
            timers.put(hash, time);
            return true;
        }
    }

    public static float time(){
        return time;
    }

    public static void resetTime(float time){
        Timers.time = time;
    }

    public static void mark(){
        marks.add(TimeUtils.nanoTime());
    }

    /** A value of -1 means mark() wasn't called beforehand. */
    public static float elapsed(){
        if(marks.size == 0){
            return -1;
        }else{
            return TimeUtils.timeSinceNanos(marks.pop()) / 1000000f;
        }
    }

    /** Use normal delta time (e. g. gdx delta * 60) */
    public static synchronized void update(){
        float delta = delta();

        time += delta;
        if(time >= maxValue){
            time = 0f;
        }

        runs.begin();

        for(DelayRun run : runs){
            run.delay -= delta;

            if(run.run != null)
                run.run.run();

            if(run.delay <= 0){
                if(run.finish != null)
                    run.finish.run();
                runs.removeValue(run, true);
                Pooling.free(run);
            }
        }

        runs.end();
    }

    public static float delta(){
        return deltaimpl.get();
    }

    public static void setDeltaProvider(DeltaProvider impl){
        deltaimpl = impl;
    }

    private static int hash(Object object, String label){
        return hash(label) + object.hashCode();
    }

    private static int hash(String label){
        return label.hashCode();
    }

    static void dispose(){
        timers.clear();
        runs.clear();
    }

    public interface DeltaProvider{
        float get();
    }
}

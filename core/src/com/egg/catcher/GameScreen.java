package com.egg.catcher;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.TimeUtils;

public class GameScreen implements Screen {
    final EggCatcher game;

    Texture redEggImage;
    Texture whiteEggImage;
    Texture bucketImage;

    OrthographicCamera camera;
    Rectangle bucket;
    Map<Integer, Rectangle> eggs;
    long lastEggCatcherTime;
    int dropsGathered;

    public GameScreen(final EggCatcher gam) {
        this.game = gam;

        // load the images for the droplet and the bucket, 64x64 pixels each
        redEggImage = new Texture(Gdx.files.internal("egg_rot.png"));
        whiteEggImage = new Texture(Gdx.files.internal("egg_weiss.png"));
        bucketImage = new Texture(Gdx.files.internal("bucket.png"));

        // create the camera and the SpriteBatch
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        // create a Rectangle to logically represent the bucket
        bucket = new Rectangle();
        bucket.x = 800 / 2 - 64 / 2; // center the bucket horizontally
        bucket.y = 20; // bottom left corner of the bucket is 20 pixels above
        // the bottom screen edge
        bucket.width = 64;
        bucket.height = 64;

        // create the eggdrops array and spawn the first eggdrop
        eggs = new HashMap<Integer, Rectangle>();
        spawnEggDrop();

    }

    private void spawnEggDrop() {
        Integer key = 0;
        if (Math.random() < 0.5) {
            key = 1;
        }
        Rectangle eggdrop = new Rectangle();
        eggdrop.x = MathUtils.random(0, 800 - 64);
        eggdrop.y = 480;
        eggdrop.width = 64;
        eggdrop.height = 64;

        eggs.put(key, eggdrop);
        lastEggCatcherTime = TimeUtils.nanoTime();
    }

    @Override
    public void render(float delta) {
        // clear the screen with a dark blue color. The
        // arguments to glClearColor are the red, green
        // blue and alpha component in the range [0,1]
        // of the color to be used to clear the screen.
        Gdx.gl.glClearColor(153, 0, 255, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // tell the camera to update its matrices.
        camera.update();

        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        game.batch.setProjectionMatrix(camera.combined);

        // begin a new batch and draw the bucket and
        // all drops
        game.batch.begin();
        game.font.draw(game.batch, "EggCatchers Collected: " + dropsGathered, 0, 480);
        game.batch.draw(bucketImage, bucket.x, bucket.y);

        for (Integer key : eggs.keySet()) {
            if (key == 0) {
                game.batch.draw(redEggImage, eggs.get(key).x, eggs.get(key).y);
            } else {
                game.batch.draw(whiteEggImage, eggs.get(key).x, eggs.get(key).y);
            }


        }
        game.batch.end();

        // process user input
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - 64 / 2;
        }
        if (Gdx.input.isKeyPressed(Keys.LEFT))
            bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Keys.RIGHT))
            bucket.x += 200 * Gdx.graphics.getDeltaTime();

        // make sure the bucket stays within the screen bounds
        if (bucket.x < 0)
            bucket.x = 0;
        if (bucket.x > 800 - 64)
            bucket.x = 800 - 64;

        // check if we need to create a new eggdrop
        if (TimeUtils.nanoTime() - lastEggCatcherTime > 1000000000)
            spawnEggDrop();


        for (Iterator<Map.Entry<Integer, Rectangle>> it = eggs.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, Rectangle> entry = it.next();
            Rectangle eggdrop = entry.getValue();
            eggdrop.y -= 200 * Gdx.graphics.getDeltaTime();
            if (eggdrop.y + 64 < 0)
                it.remove();
            if (eggdrop.overlaps(bucket)) {
                if (entry.getKey() == 0)
                    dropsGathered--;
                else
                    dropsGathered++;

                it.remove();
            }
        }

    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
        // start the playback of the background music
        // when the screen is shown
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        redEggImage.dispose();
        bucketImage.dispose();
    }

}
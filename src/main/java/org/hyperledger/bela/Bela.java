/*
 *
 *  * Copyright Hyperledger Besu Contributors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 *  * the License. You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 *  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package org.hyperledger.bela;

import static com.googlecode.lanterna.input.KeyType.ArrowLeft;
import static com.googlecode.lanterna.input.KeyType.ArrowRight;
import static com.googlecode.lanterna.input.KeyType.Escape;

import org.hyperledger.besu.ethereum.storage.StorageProvider;
import org.hyperledger.besu.ethereum.storage.keyvalue.KeyValueSegmentIdentifier;
import org.hyperledger.besu.ethereum.storage.keyvalue.KeyValueStorageProviderBuilder;
import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem;
import org.hyperledger.besu.plugin.services.storage.rocksdb.RocksDBKeyValueStorageFactory;
import org.hyperledger.besu.plugin.services.storage.rocksdb.RocksDBMetricsFactory;
import org.hyperledger.besu.plugin.services.storage.rocksdb.configuration.RocksDBCLIOptions;
import org.hyperledger.besu.plugin.services.storage.rocksdb.configuration.RocksDBFactoryConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import org.hyperledger.bela.components.BlockPanel;
import org.hyperledger.bela.components.SearchForBlockPanel;
import org.hyperledger.bela.config.BelaConfigurationImpl;

public class Bela {

  public static void main(final String[] args) throws Exception {
    final Path dataDir = Paths.get(args[0]);
    System.out.println("We are loading : " + dataDir);
    final StorageProvider provider =
        createKeyValueStorageProvider(dataDir, dataDir.resolve("database"));

    BlockChainBrowser browser = BlockChainBrowser.fromProvider(provider);

    try (Terminal terminal = new DefaultTerminalFactory().createTerminal()) {
      Screen screen = new TerminalScreen(terminal);
      screen.startScreen();
      SearchForBlockPanel searchPanel = new SearchForBlockPanel();

      // Create gui and start gui
      MultiWindowTextGUI gui = new MultiWindowTextGUI(screen);
      gui.setTheme(new SimpleTheme(TextColor.ANSI.WHITE, TextColor.ANSI.BLACK));

      // Create window to hold the panel
      BasicWindow window = new BasicWindow("Bela DB Browser");
      window.setHints(List.of(Window.Hint.FULL_SCREEN));

      var blockPanelHolder = new Panel();
      Panel panel = new Panel(new BorderLayout()) {
        @Override
        public boolean handleInput(final KeyStroke key) {
          switch(key.getKeyType()) {
            case Escape -> {
              window.close();
              return true;
            }
            case ArrowRight -> {
              blockPanelHolder.removeAllComponents();
              blockPanelHolder.addComponent(browser.moveForward().blockPanel().createComponent());
              return true;
            }
            case ArrowLeft -> {
              blockPanelHolder.removeAllComponents();
              blockPanelHolder.addComponent(browser.moveBackward().blockPanel().createComponent());
              return true;
            }
          }
          return super.handleInput(key);
        }
      };

      // add summary panel
      panel.addComponent(browser.showSummaryPanel().createComponent()
          .withBorder(Borders.singleLine()), BorderLayout.Location.TOP);

      // add block detail panel
      blockPanelHolder.addComponent(browser.blockPanel().createComponent());
      panel.addComponent(blockPanelHolder, BorderLayout.Location.BOTTOM);

      window.setComponent(panel);
      System.out.println(window.getFocusedInteractable());
      gui.addWindowAndWait(window);

    }
  }

  private static StorageProvider createKeyValueStorageProvider(
      final Path dataDir, final Path dbDir) {
    return new KeyValueStorageProviderBuilder()
        .withStorageFactory(
            new RocksDBKeyValueStorageFactory(
                () ->
                    new RocksDBFactoryConfiguration(
                        RocksDBCLIOptions.DEFAULT_MAX_OPEN_FILES,
                        RocksDBCLIOptions.DEFAULT_MAX_BACKGROUND_COMPACTIONS,
                        RocksDBCLIOptions.DEFAULT_BACKGROUND_THREAD_COUNT,
                        RocksDBCLIOptions.DEFAULT_CACHE_CAPACITY),
                Arrays.asList(KeyValueSegmentIdentifier.values()),
                RocksDBMetricsFactory.PUBLIC_ROCKS_DB_METRICS))
        .withCommonConfiguration(new BelaConfigurationImpl(dataDir, dbDir))
        .withMetricsSystem(new NoOpMetricsSystem())
        .build();
  }
}

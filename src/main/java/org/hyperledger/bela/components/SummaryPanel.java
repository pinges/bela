package org.hyperledger.bela.components;

import org.hyperledger.besu.ethereum.chain.ChainHead;

import java.util.Optional;

import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import org.apache.tuweni.bytes.Bytes;

public class SummaryPanel implements LanternaComponent<Panel>{
  private final String worldStateRoot;
  private final ChainHead chainHead;
  private final Optional<Bytes> contractcode;

  public SummaryPanel(final String worldStateRoot, final ChainHead chainHead, final Optional<Bytes> contractcode) {
    this.worldStateRoot = worldStateRoot;
    this.chainHead = chainHead;
    this.contractcode = contractcode;
  }

  @Override
  public Panel createComponent() {
    Panel panel = new Panel();
    panel.setLayoutManager(new GridLayout(2));

    panel.addComponent(new Label("db world state root:"));
    panel.addComponent(new Label(worldStateRoot));

    panel.addComponent(new Label("chain head:"));
    panel.addComponent(new Label(String.valueOf(chainHead.getHeight())));

    panel.addComponent(new Label("chain head hash:"));
    panel.addComponent(new Label(chainHead.getHash().toHexString()));

    panel.addComponent(new Label("contract code"));
    panel.addComponent(new Label(contractcode.map(Bytes::toHexString).orElse("")));

    return panel;
  }
}

package ca.qc.banq.gia.authentication.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("serial")
public class AppData implements Serializable {
	private String id;
	private String displayName;
}

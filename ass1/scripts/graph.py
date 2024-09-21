import matplotlib.pyplot as plt
import os
import numpy as np

output_dir = os.path.join('..', 'output', 'results')
file_names = ['naive_server.txt', 'server_cache.txt', 'client_cache.txt']

# Prepare a color map for different plots
colors = ['b', 'g', 'r']
labels = ['Naive Server', 'Server Cache', 'Client Cache']

# Create a figure for the combined plot
plt.figure(figsize=(10, 6))

# For individual plots
for file_name, color, label in zip(file_names, colors, labels):
    file_path = os.path.join(output_dir, file_name)

    # Check if the file exists
    if not os.path.isfile(file_path):
        print(f"File not found: {file_name}. Skipping.")
        continue

    x = []
    y = []

    with open(file_path, "r") as file:
        for i, line in enumerate(file, start=1):
            if "AVERAGES PER METHOD" in line:
                break
            if 'turnaround time' in line:
                try:
                    parts = line.split("turnaround time: ")[1].split(" ms")[0]
                    turnaround_time = int(parts)

                    x.append(i)
                    y.append(turnaround_time)
                except (IndexError, ValueError):
                    print(f'Skipping line: {line}')

    # [ Binning ]
    num_bins = 50
    bin_indices = np.linspace(1, len(x), num_bins + 1).astype(int)

    x_bins = [(x[bin_indices[i] - 1] + x[bin_indices[i]]) / 2 for i in range(len(bin_indices) - 1)]
    y_bins = [np.mean(y[bin_indices[i - 1]:bin_indices[i]]) for i in range(1, len(bin_indices))]

    # Plotting the binned data on the combined plot
    plt.plot(x_bins, y_bins, marker="o", linestyle="-", color=color, label=label)

    # Create an individual plot for each file
    plt.figure(figsize=(10, 6))
    plt.plot(x_bins, y_bins, marker="o", linestyle="-", color=color)
    plt.title(f"{label} - Turnaround Time vs Query Number")
    plt.xlabel("Query Number")
    plt.ylabel("Turnaround Time (ms)")
    plt.grid(True)
    plt.tight_layout()

    # Save individual plots
    plt.savefig(os.path.join(output_dir, f"{file_name}_plot.png"))
    plt.close()  # Close the individual plot

# Customize the combined plot
plt.title("Combined Turnaround Time vs Query Number")
plt.xlabel("Query Number")
plt.ylabel("Turnaround Time (ms)")
plt.grid(True)
plt.legend()
plt.tight_layout()

# Save the combined plot
plt.savefig(os.path.join(output_dir, "combined_time_plot.png"))

plt.show()  # Show the combined plot
